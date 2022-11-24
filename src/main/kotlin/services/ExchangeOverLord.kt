package services

import configs.ExchangeConfig
import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.YieldingWaitStrategy
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import constants.enums.ExchangeName
import models.Order
import models.OrderBook
import models.Position
import mu.KotlinLogging
import utils.TimerCallBack
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread

enum class DaemonThreadFactory : ThreadFactory {

    INSTANCE;

    override fun newThread(r: Runnable): Thread {
        val t = Thread(r)
        t.isDaemon = true
        t.priority = Thread.MAX_PRIORITY
        return t
    }
}

class Call {
    var orderBookUpdateCall: OrderBookUpdateCall? = null
    var orderResponseCall: OrderResponseCall? = null
    var positionUpdateCall: PositionUpdateCall? = null
    var walletUpdateCall: WalletUpdateCall? = null
    var cancelRejectCall: CancelRejectCall? = null
    fun clear() {
        orderBookUpdateCall = null
        orderResponseCall = null
        positionUpdateCall = null
        walletUpdateCall = null
        cancelRejectCall = null
    }
}

data class OrderBookUpdateCall(val book: OrderBook)
data class OrderResponseCall(val order: Order)
data class PositionUpdateCall(val position: Position)
data class WalletUpdateCall(val position: Position)
data class CancelRejectCall(val orderIds: List<String>, val exchangeId: String)

class MessageFactory : EventFactory<Call> {
    override fun newInstance(): Call {
        return Call()
    }
}

object ExchangeOverLord : IPrivateExchangeListener, IPublicExchangeListener {

    private val logger = KotlinLogging.logger {}
    var bookBindings = HashMap<ExchangeName, HashMap<String, MutableList<IPublicExchangeListener>>>()
    var privateBindings = HashMap<String, MutableList<IPrivateExchangeListener>>()
    var emptyQueueBindings = ArrayList<(Boolean) -> Unit>()


    private val disruptor = Disruptor(
        MessageFactory(), 262144 * 2,
        DaemonThreadFactory.INSTANCE, ProducerType.MULTI, YieldingWaitStrategy()
    )

    private val ringBuffer = disruptor.ringBuffer

    init {

        OrderManager.init()
        MarketDataManager.init()

        MarketDataManager.bind(this)
        OrderManager.bind(this)

        disruptor.handleEventsWith(EventHandler { event: Call, sequence: Long, endOfBatch: Boolean ->
            processEvent(
                event,
                sequence,
                endOfBatch
            )
        })

        TimerCallBack.start()
    }

    fun bind(
        exchangeName: ExchangeName, currency: String,
        listener: IPublicExchangeListener
    ) {
        if (exchangeName !in bookBindings) {
            bookBindings[exchangeName] = HashMap()
        }

        if (!bookBindings[exchangeName]!!.containsKey(currency)) {
            bookBindings[exchangeName]!![currency] = mutableListOf()
        }

        bookBindings[exchangeName]!![currency]!!.add(listener)
        MarketDataManager.subscribe(exchangeName, currency)
    }

    fun bindPrivate(
        exchangeId: String, currency: String,
        listener: IPrivateExchangeListener
    ) {

        if (exchangeId !in privateBindings) {
            privateBindings[exchangeId] = mutableListOf()
        }
        privateBindings[exchangeId]!!.add(listener)
        OrderManager.subscribe(exchangeId, currency)
    }


    fun emptyQueueBind(onEmptyQueue: (Boolean) -> Unit) {
        emptyQueueBindings.add(onEmptyQueue)
    }

    override fun onOrderBookUpdation(book: OrderBook) {
        try {
            val orderBook = book.copy()
            ringBuffer.publishEvent { event, _ ->
                event.orderBookUpdateCall = OrderBookUpdateCall(orderBook)
                event.orderResponseCall = null
                event.positionUpdateCall = null
                event.walletUpdateCall = null
                event.cancelRejectCall = null
            }

        } catch (exception: Exception) {
            logger.error(exception) { "Exception while processing on Book event" }
        }
    }

    override fun onOrderResponse(order: Order) {
        try {
            ringBuffer.publishEvent { event, _ ->
                event.orderResponseCall = OrderResponseCall(order)
                event.orderBookUpdateCall = null
                event.positionUpdateCall = null
                event.walletUpdateCall = null
                event.cancelRejectCall = null
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Exception while processing on Order Response" }
        }
    }

    override fun onPositionUpdate(position: Position) {
        try {
            ringBuffer.publishEvent { event, _ ->
                event.positionUpdateCall = PositionUpdateCall(position)
                event.orderBookUpdateCall = null
                event.orderResponseCall = null
                event.walletUpdateCall = null
                event.cancelRejectCall = null
            }

        } catch (exception: Exception) {
            logger.error(exception) { "Exception while processing on Position Update" }
        }
    }

    override fun onWalletUpdate(position: Position) {
        try {
            ringBuffer.publishEvent { event, _ ->
                event.walletUpdateCall = WalletUpdateCall(position)
                event.orderBookUpdateCall = null
                event.orderResponseCall = null
                event.positionUpdateCall = null
                event.cancelRejectCall = null
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Exception while processing on Wallet Update" }
        }
    }

    override fun onCancelReject(orderIds: List<String>, exchangeId: String) {
        try {
            ringBuffer.publishEvent { event, _ ->
                event.cancelRejectCall = CancelRejectCall(orderIds, exchangeId)
                event.walletUpdateCall = null
                event.orderBookUpdateCall = null
                event.orderResponseCall = null
                event.positionUpdateCall = null
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Exception while processing on Cancel Reject" }
        }
    }

    var gotData = false

    fun processEvent(call: Call, sequence: Long, endOfBatch: Boolean) {
        try {
            if (call.orderBookUpdateCall != null) {
                val res = processOrderBookQueue(call.orderBookUpdateCall!!)
                gotData = gotData || res
            } else if (call.orderResponseCall != null) {
                processOrderResponseQueue(call.orderResponseCall!!)
                gotData = true
            } else if (call.positionUpdateCall != null) {
                processPositionUpdateQueue(call.positionUpdateCall!!)
            } else if (call.walletUpdateCall != null){
                processWalletUpdateQueue(call.walletUpdateCall!!)
            } else {
                processCancelRejectQueue(call.cancelRejectCall!!)
            }

            if (gotData && endOfBatch) {
                try {
                    if (emptyQueueBindings.isEmpty()) {
                        return
                    }
                    emptyQueueBindings.forEach {
                        try {
                            it(endOfBatch)
                        } catch (exception: Exception) {
                            logger.error(exception) { "Exception while processing event in empty queue binding" }
                        }
                    }

                } finally {
                    gotData = false
                }
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Exception while processing event" }
        } finally {
            call.clear()
        }

    }

    fun start() {

        val mdThread = thread(
            start = true,
            name = "MarketDataThread",
            priority = Thread.MAX_PRIORITY
        ) {
            MarketDataManager.start()
        }

        val orderManagerThread = thread(
            start = true,
            name = "OrderManagerThread",
            priority = Thread.MAX_PRIORITY
        ) {
            OrderManager.start()
        }

        disruptor.start()

        mdThread.join()
        orderManagerThread.join()
        val exchanges = ExchangeConfig.authExchanges
        exchanges.forEach {
            OrderManager.getBalances(it)
        }
    }

    private fun processOrderResponseQueue(call: OrderResponseCall) {
        try {
            if (privateBindings.isEmpty()) {
                return
            }
            privateBindings[call.order.exchangeId]!!.forEach{ fx ->
                fx.onOrderResponse(call.order)
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing order response queue call" }
        }
    }

    private fun processOrderBookQueue(call: OrderBookUpdateCall): Boolean {
        try {
            if (bookBindings.isEmpty()) {
                return true
            }
            bookBindings[call.book.contract.exchange]!![call.book.contract.symbol]!!.forEach { fx ->
                fx.onOrderBookUpdation(call.book)
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing order book queue call" }
        }
        return true
    }

    private fun processPositionUpdateQueue(call: PositionUpdateCall) {
        try {
            if (privateBindings.isEmpty()) {
                return
            }
            privateBindings[call.position.exchangeId]!!.forEach { fx ->
                fx.onPositionUpdate(call.position)
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing position update queue call" }
        }
    }

    private fun processWalletUpdateQueue(call: WalletUpdateCall) {
        try {
            if (privateBindings.isEmpty()) {
                return
            }
            privateBindings[call.position.exchangeId]!!.forEach { fx ->
                fx.onWalletUpdate(call.position)
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing wallet update queue call" }
        }
    }

    private fun processCancelRejectQueue(call: CancelRejectCall) {
        try {
            if (privateBindings.isEmpty()) {
                return
            }
            privateBindings[call.exchangeId]!!.forEach { fx ->
                fx.onCancelReject(call.orderIds, call.exchangeId)
            }
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing cancel reject queue call" }
        }
    }
}