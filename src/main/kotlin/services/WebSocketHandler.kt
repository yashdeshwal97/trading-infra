package services

import constants.common.Constants
import helpers.OkHttpClientHelper
import mu.KotlinLogging
import okhttp3.*
import okio.ByteString
import utils.TimerCallBack
import java.net.InetSocketAddress
import java.net.Proxy


abstract class WebSocketHandler: WebSocketListener, IExchange {

    private val logger = KotlinLogging.logger {}

    val contracts = HashSet<String>()

    var bookBindings: IPublicExchangeListener? = null
    var privateBindings: IPrivateExchangeListener? = null

    var websocket: WebSocket? = null
    var client: OkHttpClient? = null
    var lastNotificationTime = 0L
    var lastStaleCheckTime = 0L

    open val notificationTimeout = 60000
    open val staleTimeOut = Constants.unauthenticatedWebsocketStaleTimeout
    open val pingInterval = 0L

    var isConnected = false

    open val authenticated = false
    open var proxyHost: String = ""
    open var proxyPort: Int = 0

    @Volatile
    override var lastMessageTime: Long = 0

    init {
        TimerCallBack.bind(::checkStale)
    }

    var wsAddress: String
    val options: Map<String, Any>

    constructor(wsAddress: String, options: Map<String, Any>) {
        this.wsAddress = wsAddress
        this.options = options
        this.proxyHost = if("proxyHost" in options) {
            options["proxyHost"] as String
        } else {
            ""
        }
        this.proxyPort = if("proxyPort" in options) {
            options["proxyPort"] as Int
        } else {
            0
        }
    }

    override fun getHeaders(): Headers? {
        return Headers.Builder().build()
    }

    override fun subscribe(symbol: String) {
        logger.info{"Subscribing to symbol ${symbol}"}
        contracts.add(symbol)
    }

    override fun bind(IPublicExchangeListener: IPublicExchangeListener) {
        bookBindings = IPublicExchangeListener
    }

    override fun bindPrivate(IPrivateExchangeListener: IPrivateExchangeListener) {
        privateBindings = IPrivateExchangeListener
    }

    override fun canConnect(): Boolean {
        return contracts.isNotEmpty()
    }

    override fun connect() {

        val connectingWSAddress = wsAddress
        
        logger.info{"Connecting to $connectingWSAddress"}

        val proxy: Proxy = if(proxyHost != "") {
            logger.info{"${wsAddress} using proxy $proxyHost:$proxyPort"}
            Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort))
        } else {
            Proxy.NO_PROXY
        }

        client = OkHttpClientHelper.getClient(pingInterval, proxy)

        val request = Request.Builder().url(connectingWSAddress).headers(getHeaders()!!).build()

        websocket = client!!.newWebSocket(request, this)
    }



    override fun close() {
        if(websocket != null) {
            websocket?.close(1000, "Quit")
        }
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        isConnected = true
        lastMessageTime = System.currentTimeMillis()
    }


    override fun onMessage(webSocket: WebSocket, text: String) {
        lastMessageTime = System.currentTimeMillis()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        lastMessageTime = System.currentTimeMillis()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        isConnected = true
        logger.info{"OnClosed $wsAddress => $code $reason"}
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isConnected = false
        logger.error(t) {"OnFailure $wsAddress"}
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        isConnected = false
        logger.info{"OnClosed $wsAddress => $code $reason"}

        if(reason == "Quit") {
            return
        }
    }

    protected fun tryReconnect(sleep: Long = 5000) {
        logger.info{"Reconnecting to $wsAddress"}
        beforeConnect()
        connect()
    }

    open fun onStale() {
        if (websocket != null) {
            if (isConnected) {
                isConnected = false
                logger.info { "Cancelling $wsAddress websocket" }
                websocket!!.cancel()
                logger.info { "Cancelled $wsAddress" }
            } else {
                logger.info { "Websocket $wsAddress is not connected" }
                websocket!!.cancel()
                tryReconnect()
            }
        } else {
            logger.info { "Null $wsAddress websocket reconnecting..." }
            tryReconnect()
        }
    }

    fun checkStale() {

        if(canConnect()) {

            var now = System.currentTimeMillis()

            if((now - lastStaleCheckTime) > staleTimeOut
                    && (now - lastMessageTime) > staleTimeOut) {

                lastStaleCheckTime = now

                logger.info{"Stale $wsAddress data"}
                onStale()

                if(now - lastNotificationTime > notificationTimeout) {
                    lastNotificationTime = now

                }
            }
        }
    }

}