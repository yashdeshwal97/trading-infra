package services

import configs.ExchangeConfig
import constants.enums.ExchangeName
import exchanges.ftx.FTXExchange
import models.Order
import models.Position
import mu.KotlinLogging
import services.ftx.FTXOrderManager
import kotlin.concurrent.thread

object OrderManager : IPrivateExchangeListener {

    var orderMgrs: HashMap<String, IAuthExchange> = HashMap()
    var privateBindings: IPrivateExchangeListener? = null
    private val logger = KotlinLogging.logger {}


    fun init() {
        val exchanges = ExchangeConfig.authExchanges
        exchanges.forEach { exchangeId ->
            val properties = ExchangeConfig.getAuthProperties(exchangeId)
            if ("enabled" in properties && (properties["enabled"] as Boolean)) {
                val exName = ExchangeName.valueOf(properties["exchangeName"] as String)
                when (exName) {
                    ExchangeName.FTX -> initializeFTX(properties, exchangeId)
                    else -> {
                        throw Exception("Invalid config $exchangeId $exName $properties")
                    }
                }
            }
        }
    }

    fun sendNewOrder(orders: List<Order>) : Boolean {
        var sent = false
        orders.forEach { order: Order ->
            var newOrder = order.duplicate()
            val a = orderMgrs[order.exchangeId]!!.sendNewOrder(newOrder)
            sent = a || sent
        }

        return sent
    }

    fun cancelOrder(orders: List<Order>) : Boolean {
        var sent = false
        orders.forEach { order: Order ->
            var newOrder = order.duplicate()
            val a = orderMgrs[order.exchangeId]!!.cancelOrder(newOrder)
            sent = a || sent
        }

        return sent
    }

    fun cancelAll(symbol: String, exchangeId: String) {
        orderMgrs[exchangeId]!!.cancelAll(symbol)
    }

    fun getPositions(exchangeId: String) {
        orderMgrs[exchangeId]!!.getPositions()
    }

    fun getBalances(exchangeId: String) {
        orderMgrs[exchangeId]!!.getBalances()
    }

    fun bind(listener: IPrivateExchangeListener) {
        privateBindings = listener
    }


    fun subscribe(exchangeId: String, currency: String) {
        val orderManager: IExchange = orderMgrs[exchangeId] as IExchange
        orderManager!!.subscribe(currency)
    }

    override fun onOrderResponse(order: Order) {
        try {
            privateBindings!!.onOrderResponse(order)
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing Order Response" }
        }
    }

    override fun onPositionUpdate(position: Position) {
        try {
            privateBindings!!.onPositionUpdate(position)
        } catch (exception: Exception) {
            logger.error(exception) { "Error while fetching Position" }
        }
    }

    override fun onWalletUpdate(position: Position) {
//        logger.info { position.toString() }
        try {
            privateBindings!!.onWalletUpdate(position)
        } catch (exception: Exception) {
            logger.error(exception) { "Error while fetching Wallet Balance" }
        }
    }

    override fun onCancelReject(orderIds: List<String>, exchangeId: String) {
        logger.info { "Error while cancelling the following orders :- ${orderIds}"  }
        try {
            privateBindings!!.onCancelReject(orderIds, exchangeId)
        } catch (exception: Exception) {
            logger.error(exception) { "Error while fetching cancelled orders" }
        }
    }


    private fun initializeExchange(id: String, streamingExchange: IExchange) {
        streamingExchange.bindPrivate(this)
    }

    private fun initializeFTX(properties: Map<String, Any>, id: String) {

        val exchange = FTXExchange(properties)
        val orderManager = FTXOrderManager(exchange, id, properties)
        orderMgrs[id] = orderManager
        initializeExchange(id, orderManager)
    }


    fun start() {
        val threads = ArrayList<Thread>()
        orderMgrs.forEach { (id, u) ->
            val exchange = u as IExchange
            if (exchange.canConnect()) {
                exchange.beforeConnect()
                val t = thread(
                    start = true,
                    name = exchange.exchangeName.toString(),
                    priority = Thread.MAX_PRIORITY
                ) {
                    try {
                        exchange.connect()
                    } catch (exception: Exception) {
                        logger.error(exception) { "Error connecting to exchange" }
                    }
                }

                threads.add(t)
            } else {
                logger.info { "Not connecting to exchange $id ${u.exchangeName}" }
            }
        }

        threads.forEach {
            it.join()
        }
    }

}