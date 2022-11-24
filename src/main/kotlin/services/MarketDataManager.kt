package services

import configs.ExchangeConfig
import constants.enums.ExchangeName
import models.OrderBook
import mu.KotlinLogging
import services.websockets.ftx.FTX
import services.websockets.okex.OkexV5
import kotlin.concurrent.thread


object MarketDataManager : IPublicExchangeListener {

    private val logger = KotlinLogging.logger {}

    val streamingExchanges = HashMap<ExchangeName, IExchange>()
    var bookBindings: IPublicExchangeListener? = null
    var connectionCount = 1

    fun init() {

        logger.info { "Initializing MD ${ExchangeConfig.mdExchanges}" }

        connectionCount = ExchangeConfig.mdConnectionCount

        val exchanges = ExchangeConfig.mdExchanges

        exchanges.forEach { it ->
            val properties = ExchangeConfig.getMDProperties(it)
            logger.info { "Initializing $it $properties" }
            when (it) {
                ExchangeName.OKEXV5 -> initializeOkexV5(properties)
                ExchangeName.FTX -> initializeFTX(properties)
                else -> {
                    throw Exception("Invalid MD exchange $it $properties")
                }
            }
        }
    }

    private fun initializeExchange(exchangeName: ExchangeName, initializer: () -> IExchange) {
        val streamingExchange = initializer()
        streamingExchange.bind(this)
        streamingExchanges[exchangeName] = streamingExchange
    }


    private fun initializeOkexV5(properties: Map<String, Any>) {
        val exchangeName = ExchangeName.OKEXV5
        initializeExchange(exchangeName) {
            OkexV5(properties)
        }
    }


    private fun initializeFTX(properties: Map<String, Any>) {
        val exchangeName = ExchangeName.FTX

        val arb = if ("arb" in properties) {
            properties["arb"] as Boolean
        } else {
            false
        }

        if (arb) {
            val propsOB = HashMap(properties)
            propsOB["ob"] = true
            initializeExchange(exchangeName) {
                FTX(propsOB)
            }

            val propsTicker = HashMap(properties)
            propsTicker["ob"] = false
            initializeExchange(exchangeName) {
                FTX(propsTicker)
            }

        } else {
            initializeExchange(exchangeName) {
                FTX(properties)
            }
        }
    }


    override fun onOrderBookUpdation(book: OrderBook) {
        try {
            bookBindings!!.onOrderBookUpdation(book)
        } catch (exception: Exception) {
            logger.error(exception) { "Error while processing order book bindings" }
        }
    }

    fun bind(listener: IPublicExchangeListener) {
        bookBindings = listener
    }

    fun subscribe(exchangeName: ExchangeName, currency: String) {
        val exchange = streamingExchanges[exchangeName]!!
        exchange.subscribe(currency)
    }

    fun start() {
        val threads = ArrayList<Thread>()
        streamingExchanges.forEach { (t, u) ->
            if (u.canConnect()) {
                val t = thread(
                    start = true,
                    name = u.exchangeName.toString(),
                    priority = Thread.MAX_PRIORITY
                ) {
                    try {
                        u.connect()
                    } catch (exception: Exception) {
                        logger.error(exception) { "Error connecting to exchange" }
                    }
                }

                threads.add(t)
            } else {
                logger.info { "Not connecting to exchange $t" }
            }
        }

        threads.forEach {
            it.join()
        }
    }
}