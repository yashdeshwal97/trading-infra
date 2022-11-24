package authInterceptors.ftx

import authInterceptors.BaseInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import utils.BaseInterceptorUtils
import utils.ftx.FTXUtils

class FTXAuthInterceptor : BaseInterceptor {

    constructor(secret: String) : super(secret, BaseInterceptorUtils.HmacSHA256)

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val ts = request.header(FTXUtils.HEADER_TS)!!
        val method = request.method.toUpperCase()

        val path = request.url.encodedPath
        val query = request.url.query
        val fullPath = path + if (query != null && query != "") {
            "?$query"
        } else {
            ""
        }

        val body = getRequestBody(request)

        val payload = "$ts$method$fullPath$body"

        val signature = signature(payload)

        val newRequestBuilder = request
            .newBuilder()
            .header(FTXUtils.HEADER_SIGNATURE, signature)

        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }

    override fun signature(payload: String) : String {
        return hexEncodedSignature(payload)
    }

}