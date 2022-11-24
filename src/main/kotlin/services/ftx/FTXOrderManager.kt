package services.ftx

import authInterceptors.ftx.FTXAuthInterceptor
import constants.enums.*
import exchanges.ftx.FTXAccountService
import exchanges.ftx.FTXExchange
import helpers.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.Order
import mu.KotlinLogging
import okhttp3.Response
import okhttp3.WebSocket
import models.ftx.requests.FTXOrderRequest
import exchanges.ftx.FTXTradeService
import services.RateLimiter
import services.IAuthExchange
import services.WebSocketHandler
import utils.TimerCallBack

class FTXOrderManager : WebSocketHandler, IAuthExchange {
    private val logger = KotlinLogging.logger {}
    private val exchange: FTXExchange
    private val tradeService: FTXTradeService
    private val accountService: FTXAccountService
    val myPingInterval = 10000L
    var myLastPingTime = 0L
    val exchangeID: String
    override val authenticated = true
    override val exchangeName: ExchangeName = ExchangeName.FTX
    var orderStateManager = FTXOrderStateManager()
    var rateLimiter: RateLimiter

    constructor(exchange: FTXExchange, exchangeID: String, options: Map<String, Any> = hashMapOf()) :
            super("wss://ftx.com/ws/", options) {
        logger.info { "Initializing FTX Streaming Exchange" }
        this.exchange = exchange
        this.exchangeID = exchangeID
        tradeService = exchange.tradeService as FTXTradeService
        accountService = exchange.accountService as FTXAccountService
        rateLimiter = RateLimiter(
            (options["count"] as Number).toInt(),
            (options["intervalSeconds"] as Number).toDouble()
        )
        TimerCallBack.bind {
            ping()
        }
    }

    override fun beforeConnect() {
        orderStateManager.sentOrders.forEach { (_, order) ->
            cancelOrder(order)
        }
    }

    override fun sendNewOrder(order: Order): Boolean {
        if (!rateLimiter.callAllowed(1)) {
            order.status = OrderStatus.REJECTED
            logger.info { "RATE_LIMITER_ORDER_REJECTION: " + order.toString() }
            privateBindings!!.onOrderResponse(order)
            return false
        }
        orderStateManager.addNewOrder(order)
        rateLimiter.addCall(1)
        GlobalScope.launch(Dispatchers.IO) {
            val side = if (order.side == Side.BID) "buy" else "sell"
            val price = order.price
            val type = "limit"
            val size = order.amount
            val reduceOnly = order.reduceOnly
            val ioc = order.ioc
            val postOnly = order.postOnly
            val clientId = order.id
            val symbol = order.symbol
            val request = FTXOrderRequest(
                symbol, side, price, type, size, reduceOnly,
                ioc, postOnly, clientId
            )
            try {
                val response = tradeService!!.placeNewOrder(request)
                val newOrder = orderStateManager.receiveNewOrderUpdate(response)
                if (newOrder != null) {
                    logger.info { "REST_NEW_ORDER: " + newOrder.toString() }
                    privateBindings!!.onOrderResponse(newOrder)
                }
            } catch (e: Exception) {
                logger.error(e) { "Exception while sending new orders" }
                order.status = OrderStatus.REJECTED
                orderStateManager.deleteOrder(order.id)
                privateBindings!!.onOrderResponse(order)
            }
        }
        return true
    }

    override fun cancelOrder(order: Order): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response =
                    tradeService!!.cancelByClientId(order.id)
                val newOrder = orderStateManager.receiveCancelOrderUpdate(response, order.id)
                if (newOrder != null) {
                    logger.info { "REST_CANCEL_ORDER" + newOrder.toString() }
                    privateBindings!!.onOrderResponse(newOrder)
                }
            } catch (e: Exception) {
                logger.error(e) { "Exception while cancelling orders" }
                privateBindings!!.onOrderResponse(order)
            }
        }
        return true
    }

    override fun cancelAll(symbol: String): Boolean {
        val rejectedOrders = orderStateManager.sentOrders.filter { (_, order) ->
            order.symbol == symbol
        }.values.toList()
        val rejectedIds = mutableListOf<String>()
        for(order in rejectedOrders) {
            rejectedIds.add(order.id)
        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response =
                    tradeService!!.cancelAll(symbol)
                if (!response.isSuccess) {
                    logger.error { "All Order cancellation failed" }
                    privateBindings!!.onCancelReject(rejectedIds, exchangeID)
                }
            } catch (e: Exception) {
                logger.error(e) { "Exception while cancelling orders" }
                privateBindings!!.onCancelReject(rejectedIds, exchangeID)
            }
        }
        return true
    }

    override fun getPositions() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = accountService!!.getPositions()
                val positions = orderStateManager.receivePositionUpdate(response, exchangeID)
                if (positions != null) {
                    positions.forEach {
                        privateBindings!!.onPositionUpdate(it)
                    }
                }
//                logger.info { response }
            } catch (e: Exception) {
                logger.error { "Exception while fetching positions" }
            }
        }
    }

    override fun getBalances() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = accountService!!.balances
                val positions = orderStateManager.receiveWalletUpdate(response, exchangeID)
                if (positions != null) {
                    positions.forEach {
                        logger.info { it }
                        privateBindings!!.onWalletUpdate(it)
                    }
                }
               // logger.info { response }
            } catch (e: Exception) {
                logger.error { "Exception while fetching positions" }
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {

        super.onOpen(webSocket, response)
        logger.info { "FTX Connection Open" }
        sendAuthRequest(webSocket)
    }

    fun sendAuthRequest(webSocket: WebSocket) {
        val key = options["apiKey"]
        val time = System.currentTimeMillis()
        val payload = "${time}websocket_login"
        val sign = FTXAuthInterceptor(options["secret"] as String).signature(payload)

        val subaccount = if ("subaccount" in options) {
            options["subaccount"]
        } else {
            null
        }

        webSocket.send(
            Helpers.mapToJsonString(
                mapOf(
                    "op" to "login",
                    "args" to mapOf(
                        "key" to key,
                        "sign" to sign,
                        "time" to time,
                        "subaccount" to subaccount
                    )
                )
            )
        )

        webSocket.send(
            Helpers.mapToJsonString(
                mapOf(
                    "op" to "subscribe", "channel" to "orders"
                )
            )
        )
        webSocket.send(
            Helpers.mapToJsonString(
                mapOf(
                    "op" to "subscribe", "channel" to "fills"
                )
            )
        )
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        val message = Helpers.textToJson(text) as Map<*, *>
        if ("channel" in message) {
            when (message["channel"] as String) {
                "orders" -> {
                    val type = message["type"] as String
                    if (type == "update") {
                        onOrderResponse(message)
                    }
                }
            }
        }
    }

    private fun onOrderResponse(message: Map<*, *>) {
        val data = message["data"] as Map<*, *>
        val newOrder = orderStateManager.receiveOrderUpdate(data)
        if (newOrder != null) {
            logger.info { "WEBSOCKET_ORDER_UPDATE: " + newOrder.toString() }
            privateBindings!!.onOrderResponse(newOrder)
        }
    }

    fun ping() {
        val now = System.currentTimeMillis()
        if (now - myLastPingTime > myPingInterval) {
            if (websocket != null) {
                websocket!!.send(Helpers.mapToJsonString(mapOf("op" to "ping")))
                myLastPingTime = now
            }
        }
    }

}