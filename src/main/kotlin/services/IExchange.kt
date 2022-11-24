package services

import constants.enums.ExchangeName
import okhttp3.Headers
import java.io.Closeable

interface IExchange : Closeable {
    val exchangeName: ExchangeName
    var lastMessageTime: Long
    fun subscribe(symbol: String)
    fun bind(IPublicExchangeListener: IPublicExchangeListener)
    fun bindPrivate(IPrivateExchangeListener: IPrivateExchangeListener)
    fun connect()
    fun canConnect(): Boolean
    fun getHeaders(): Headers?
    fun beforeConnect()
}