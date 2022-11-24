package exchanges.base

import retrofit2.Call

fun <T> Call<T>.executeCall() : T {

    val response = this.execute()

    if (response.isSuccessful) {
        val rBody = response.body()
        if (rBody != null) {
            return rBody
        } else {
            throw Exception("Body is null")
        }
    } else {
        throw Exception("Call failed ${response.code()} : ${response.errorBody()?.string()}")
    }
}
