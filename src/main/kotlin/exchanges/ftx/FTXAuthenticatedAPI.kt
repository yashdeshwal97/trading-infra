package exchanges.ftx

import models.ftx.account.FTXPosition
import models.ftx.trade.FTXOrder
import models.ftx.requests.FTXCancelRequest
import models.ftx.requests.FTXOrderRequest
import models.ftx.FTXResult
import models.ftx.account.FTXAccount
import retrofit2.Call
import retrofit2.http.*

interface FTXAuthenticatedAPI {


    @GET("/api/account")
    fun getAccount(@Header("FTX-KEY") key: String,
                   @Header("FTX-TS") timestamp: String,
                   @Header("FTX-SUBACCOUNT") subaccount: String?): Call<FTXResult<FTXAccount>>

    @GET("/api/wallet/balances")
    fun getBalances(@Header("FTX-KEY") key: String,
                    @Header("FTX-TS") timestamp: String,
                    @Header("FTX-SUBACCOUNT") subaccount: String?): Call<FTXResult<List<Map<String, Any>>>>

    @GET("/api/wallet/all_balances")
    fun getAllBalances(@Header("FTX-KEY") key: String,
                       @Header("FTX-TS") timestamp: String,
                       @Header("FTX-SUBACCOUNT") subaccount: String?): Call<FTXResult<Map<String, Any>>>

    @GET("/api/wallet/deposits")
    fun getDeposits(@Header("FTX-KEY") key: String,
                    @Header("FTX-TS") timestamp: String,
                    @Header("FTX-SUBACCOUNT") subaccount: String?,
                    @Header("start_time") startTime: Long?,
                    @Header("end_time") endTime: Long?): Call<FTXResult<List<Map<String, Any>>>>

    @GET("/api/wallet/withdrawals")
    fun getWithdrawals(@Header("FTX-KEY") key: String,
                       @Header("FTX-TS") timestamp: String,
                       @Header("FTX-SUBACCOUNT") subaccount: String?,
                       @Header("start_time") startTime: Long?,
                       @Header("end_time") endTime: Long?): Call<FTXResult<List<Map<String, Any>>>>

    @GET("/api/orders/by_client_id/{id}")
    fun getOrderByClientId(@Header("FTX-KEY") key: String,
                           @Header("FTX-TS") timestamp: String,
                           @Header("FTX-SUBACCOUNT") subaccount: String?,
                           @Path("id") id: String): Call<FTXResult<FTXOrder>>

    @GET("/api/positions")
    fun getPositions(@Header("FTX-KEY") key: String,
                     @Header("FTX-TS") timestamp: String,
                     @Header("FTX-SUBACCOUNT") subaccount: String?): Call<FTXResult<List<FTXPosition>>>

    @POST("/api/orders")
    fun placeNewOrder(@Header("FTX-KEY") key: String,
                      @Header("FTX-TS") timestamp: String,
                      @Header("FTX-SUBACCOUNT") subaccount: String?,
                      @Body request: FTXOrderRequest
    ): Call<FTXResult<FTXOrder>>

    @HTTP(method = "DELETE", path = "/api/orders/by_client_id/{id}", hasBody = true)
    fun cancelByClientId(@Header("FTX-KEY") key: String,
                         @Header("FTX-TS") timestamp: String,
                         @Header("FTX-SUBACCOUNT") subaccount: String?,
                         @Path("id") clientId: String): Call<FTXResult<String>>

    @HTTP(method = "DELETE", path = "/api/orders", hasBody = true)
    fun cancelAll(@Header("FTX-KEY") key: String,
                  @Header("FTX-TS") timestamp: String,
                  @Header("FTX-SUBACCOUNT") subaccount: String?,
                  @Body request: FTXCancelRequest
    ): Call<FTXResult<String>>

    @HTTP(method = "GET", path = "/api/fills")
    fun fills(@Header("FTX-KEY") key: String,
              @Header("FTX-TS") timestamp: String,
              @Header("FTX-SUBACCOUNT") subaccount: String?,
              @Query("market") market: String?,
              @Query("start_time") startTime: Long?,
              @Query("end_time") endTime: Long?): Call<FTXResult<List<Map<String, Any>>>>

}