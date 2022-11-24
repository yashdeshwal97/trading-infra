package helpers

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.net.Proxy
import java.util.concurrent.TimeUnit

object OkHttpClientHelper {

    val okHttpClient = OkHttpClient()

    fun getClient() : OkHttpClient {

        return okHttpClient.newBuilder()
                .connectionPool(ConnectionPool(50, 5, TimeUnit.MINUTES))
                .build()
    }

    fun getClient(pingInterval: Long, proxy: Proxy): OkHttpClient {
        return okHttpClient.newBuilder().pingInterval(pingInterval, TimeUnit.SECONDS)
            .proxy(proxy).retryOnConnectionFailure(true).build()
    }
}