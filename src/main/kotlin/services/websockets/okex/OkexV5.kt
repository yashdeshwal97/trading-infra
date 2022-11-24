package services.websockets.okex

import constants.enums.ExchangeName
import helpers.Helpers
import models.OrderBook
import mu.KotlinLogging
import okhttp3.Response
import okhttp3.WebSocket
import parser.okex.OkexParser
import services.ContractManager
import services.WebSocketHandler
import services.OrderBookManager
import utils.TimerCallBack

class OkexV5: WebSocketHandler {

    private val logger = KotlinLogging.logger {}

    private var bookManager:  OrderBookManager? = null

    private var lastPingTime: Long = 0

    override val exchangeName: ExchangeName = ExchangeName.OKEXV5

    var id = 0
        get() {
            return field++
        }

    constructor(options: Map<String, Any> = hashMapOf()) :
            super(if("aws" in options && options["aws"] as Boolean) {
                "wss://wsaws.okex.com:8443/ws/v5/public"
            } else {
              //  "wss://ws.okex.com:8443/ws/v5/public"
                "wss://ws.okx.com:8443/ws/v5/public"
            }, options) {

        logger.info{"Initializing Okex Streaming Exchange"}
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {

        super.onOpen(webSocket, response)
        logger.info{"Okex Connection Open"}
        bookManager = OrderBookManager(contracts.map { ContractManager.getContract(exchangeName, it) })

        listOf("trades", "books").forEach { ch ->
            val subscription = contracts.map { symbol ->
                mapOf("channel" to ch, "instId" to symbol)
            }

            val sub = Helpers.mapToJsonString(mapOf("op" to "subscribe",
                "args" to ArrayList(subscription)))

            logger.info{"Sending subscription => $sub"}

            if(webSocket.send(sub)) {
                logger.info{"Sent subscription => $sub"}
            }
        }

    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        if (text != "pong") {
            val message = Helpers.textToJson(text)
            if (message is Map<*, *>) {
                if ("arg" in message && "data" in message && "op" !in message) {
                    val args = message["arg"] as Map<*, *>
                    val channel = args["channel"]

                    when(channel) {
                        "books" -> {
                            val data = message["data"] as List<Map<*, *>>
                            val action = message["action"] as String
                            val instrument = args["instId"] as String
                            onBookUpdate(instrument, action, data)
                        }
                    }
                }
            }
        }
        ping(webSocket)
    }

    private fun ping(webSocket: WebSocket) {
        val now = System.currentTimeMillis()
        if (now - lastPingTime > 20000) {
            webSocket!!.send("ping")
            lastPingTime = now

        }
    }

    private fun onBookUpdate(instrument: String, action: String, data: List<Map<*, *>>) {
        val snapshot = OkexParser.onSnapshot(instrument, action, data)
        if (action == "snapshot") {
            bookManager!!.onSnapshot(snapshot, instrument)
        } else {
            bookManager!!.onUpdates(snapshot, instrument)
        }
        if(instrument != null) {
            bookBindings!!.onOrderBookUpdation(bookManager!!.orderBooks[instrument]!!)
        }
    }

    override fun beforeConnect() {
    }

    companion object {

        val logger = KotlinLogging.logger {}

        @JvmStatic fun print(exchangeName: ExchangeName, currency: String, book: OrderBook)
        {
            logger.info{book.toString(3)}
        }

        @JvmStatic fun main(args: Array<String>) {
            val okex1 = OkexV5(mapOf())
//            ContractManager.initializeOkexv5()
            okex1.subscribe("BTC-USDT")
//            okex1.bind(Companion::print)
            TimerCallBack.start()
            okex1.connect()
        }
    }
}