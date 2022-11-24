package exchanges.ftx

import authInterceptors.ftx.FTXAuthInterceptor
import exchanges.Exchange
import helpers.OkHttpClientHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

open class FTXBaseService(ex: Exchange)  {

    protected val exchange: Exchange = ex
    protected val api: FTXAPI
    protected val apiAuthenticated: FTXAuthenticatedAPI?
    protected var authenticated = false
    protected val apiKey: String
    protected val secret: String
    protected var subaccount: String? = null


    init {
        val specification = exchange!!.getDefaultExchangeSpecification()
        if(specification.apiKey != null && specification.secretKey != null ) {
            apiKey = specification.apiKey!!
            secret = specification.secretKey!!
            authenticated = true
        } else {
            apiKey = ""
            secret = ""
            authenticated = false
        }


        val client = OkHttpClientHelper.getClient()

        val retrofit = Retrofit.Builder()
            .baseUrl(exchange!!.getDefaultExchangeSpecification().sslUri!!)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        api = retrofit.create(FTXAPI::class.java)

        if(authenticated) {

            val clientAuthenticated = OkHttpClient().newBuilder()
                .addInterceptor(FTXAuthInterceptor(secret))
                .build()

            val retrofitAuthenticated = Retrofit.Builder()
                .baseUrl(exchange!!.getDefaultExchangeSpecification().sslUri!!)
                .client(clientAuthenticated)
                .addConverterFactory(JacksonConverterFactory.create())
                .build()

            apiAuthenticated = retrofitAuthenticated.create(FTXAuthenticatedAPI::class.java)

            val extra = specification.exchangeSpecificParameters

            subaccount = if (extra.containsKey("subaccount")) {
                extra["subaccount"] as String?
            } else {
                null
            }
        } else {
            apiAuthenticated = null
        }
    }
}
