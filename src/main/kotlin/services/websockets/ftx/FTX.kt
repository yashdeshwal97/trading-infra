package services.websockets.ftx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import constants.enums.ExchangeName
import services.ContractManager

import helpers.Helpers
import models.OrderBook
import mu.KotlinLogging
import okhttp3.Response
import okhttp3.WebSocket
import parser.ftx.FTXParser
import services.WebSocketHandler
import services.OrderBookManager
import utils.TimerCallBack

class FTX : WebSocketHandler {


    private val logger = KotlinLogging.logger {}

    var mapper = jacksonObjectMapper()

    private var orderBookManager: OrderBookManager? = null

    private val ob: Boolean

    override val exchangeName: ExchangeName = ExchangeName.FTX


    constructor(options: Map<String, Any> = hashMapOf()): super("wss://ftx.com/ws/", options) {

        logger.info{"Initializing FTX Streaming Exchange"}

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        this.ob = if("ob" in options) {
            options["ob"] as Boolean
        } else {
            true
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {

        super.onOpen(webSocket, response)

        logger.info { "FTX Connection Open" }

        orderBookManager = OrderBookManager(contracts.map { ContractManager.getContract(exchangeName, it) })

         contracts.forEach(){

            webSocket.send(Helpers.mapToJsonString(mapOf("op" to "subscribe",
                "channel" to "trades",
                "market" to it)))

            if(this.ob) {
                webSocket.send(Helpers.mapToJsonString(mapOf("op" to "subscribe",
                    "channel" to "orderbook",
                    "market" to it)))
            } else {
                webSocket.send(Helpers.mapToJsonString(mapOf("op" to "subscribe",
                    "channel" to "ticker",
                    "market" to it)))
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {

        super.onMessage(webSocket, text)
        val message = Helpers.textToJson(text) as Map<*, *>
        if ("channel" in message) {
            when (message["channel"] as String) {

                "orderbook" -> {
                    val type = message["type"] as String
                    if (type == "update" || type == "partial") {
                        onOrderBook(message)
                    }
                }
            }
        }
        this.ping(webSocket)
    }

    fun onOrderBook(message: Map<*, *>) {
        val symbol = message["market"]!! as String
        val type = message["type"] as String
        val snapshot = FTXParser.onSnapshot(message)

        if (type == "partial") {
            orderBookManager!!.onSnapshot(snapshot, symbol)
        }

        if (type == "update") {
            orderBookManager!!.onUpdates(snapshot, symbol)
        }

        if(symbol != null) {
            bookBindings!!.onOrderBookUpdation(orderBookManager!!.orderBooks[symbol]!!)
        }
    }


    var lastPingTime = 0L

    fun ping(webSocket: WebSocket) {
        val now = System.currentTimeMillis()
        if(now - lastPingTime > 15000) {
            webSocket.send(Helpers.mapToJsonString(mapOf("op" to "ping")))
            lastPingTime = now
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
            val ftx = FTX(mapOf("apiKey" to "",
                "secret" to ""))

//          ContractManager.initializeFTX()
            ftx.subscribe("1INCH-PERP")
//            ftx.bind(Companion::print)
            TimerCallBack.start()
            ftx.connect()
        }
    }
}