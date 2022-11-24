package authInterceptors

import okhttp3.Interceptor
import okhttp3.Request
import okio.Buffer
import org.apache.commons.codec.binary.Hex
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

abstract class BaseInterceptor : Interceptor {

    private val threadLocalMac: ThreadLocal<Mac?>

    constructor(secret: String, algorithm: String) : this(secret.toByteArray(), algorithm)

    constructor(secret: ByteArray, algorithm: String) {
        val secretKey = SecretKeySpec(secret, algorithm)

        threadLocalMac = object : ThreadLocal<Mac?>() {
            override fun initialValue(): Mac? {
                return try {
                    val mac = Mac.getInstance(algorithm)
                    mac.init(secretKey)
                    mac
                } catch (e: InvalidKeyException) {
                    throw IllegalArgumentException("Invalid key for hmac initialization.", e)
                } catch (e: NoSuchAlgorithmException) {
                    throw RuntimeException(
                        "Illegal algorithm for post body digest. Check the implementation.")
                }
            }
        }
    }

    fun getRequestBody(request: Request) : String {
        val body = request.body
        return if(body != null) {
            val sink = Buffer()
            body.writeTo(sink)
            sink.readUtf8()
        } else {
            ""
        }
    }

    fun hexEncodedSignature(payload: String) : String {
        return this.hexEncodedSignature(payload.toByteArray())
    }

    fun hexEncodedSignature(payload: ByteArray) : String {
        return String(Hex.encodeHex(threadLocalMac.get()!!.doFinal(payload)))
    }

    fun base64EncodedSignature(payload: String) : String {
        return this.base64EncodedSignature(payload.toByteArray())
    }

    fun base64EncodedSignature(payload: ByteArray) : String {
        return Base64.getEncoder().encodeToString(threadLocalMac.get()!!.doFinal(payload))
    }

    abstract fun signature(payload: String) : String

}