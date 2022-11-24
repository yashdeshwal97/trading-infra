package exchanges.ftx

import models.ftx.FTXResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FTXAPI {

    @GET("/api/futures/{future_name}/stats")
    fun getFutureStats(@Path("future_name") futureName: String): Call<FTXResult<Map<String, Any?>>>

    @GET("/api/funding_rates")
    fun getFundingRates(): Call<FTXResult<List<Map<String, Any?>>>>
}