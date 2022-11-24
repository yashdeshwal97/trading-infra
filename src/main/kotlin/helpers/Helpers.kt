package helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.opencsv.CSVReader
import constants.common.Constants
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.FileReader
import java.io.FileWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.math.*

object Helpers{

    fun getDataFromUrl(urlStr: String, parameters: List<Pair<String, Any?>>? = null): Any? {
        val client = OkHttpClient()

        var urlBuilder = urlStr.toHttpUrlOrNull()!!.newBuilder()

        if(parameters != null){
            parameters?.forEach { urlBuilder.addQueryParameter(it.first, it.second.toString()) }
        }

        val url = urlBuilder.build()

        val request  = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if(response.isSuccessful){
            val data = response.body!!.string()
            val json = textToJson(data)
            return json
        } else {
            return null
        }
    }

    fun roundToBase(value: Double, base: Double): Double {
        val bValue = BigDecimal.valueOf(value / base).
                setScale(0, RoundingMode.HALF_UP) *
                BigDecimal.valueOf(base)
        return bValue.toDouble()
    }

    fun roundToBaseApprox(value: Double, base: Double): Double {
        return round(value / base) * base
    }

    val eps = BigDecimal.valueOf(100000000.0)

    fun ceilToBase(value: Double, base: Double): Double {
        return -floorToBase(-value, base)
    }

    fun ceilToBaseApprox(value: Double, base: Double): Double {
        return ceil(value / base - base / 100000000.0) * base
    }

    fun floorToBase(value: Double, base: Double): Double {
        val b = BigDecimal.valueOf(base)
        val bValue = (BigDecimal.valueOf(value).divide(b)).setScale(0, RoundingMode.FLOOR).multiply(b)

        return bValue.toDouble()
    }

    fun floorToBaseApprox(value: Double, base: Double): Double {
        return floor(value / base + base / 100000000.0) * base
    }

    private val mapper = jacksonObjectMapper()

    fun textToJson(txt: String): Any {
        return mapper.readValue(txt)
    }

    fun mapToJsonString(map: Map<String, Any?>): String {
        return mapper.writeValueAsString(map)
    }

    fun listToJsonString(list: List<Any?>) : String {
        return mapper.writeValueAsString(list)
    }

    fun toJsonString(obj: Any) : String {
        return mapper.writeValueAsString(obj)
    }

    fun satoshiCompare(a: Double, b: Double) : Int {
        var diff = a - b
        var absDiff = Math.abs(diff)

        return when {
            absDiff < Constants.ONESATOSHI -> 0
            diff > 0 -> 1
            else -> -1
        }
    }

    fun readCSV(filename: String): List<Map<String, String>> {

        var reader = CSVReader(FileReader(filename))
        var rows = ArrayList<HashMap<String, String>>()
        var header = ArrayList<String>()
        var idx = 0

        reader.forEach {
            if(idx == 0){
                for(x in it){
                    header.add(x)
                }
            } else {
                var row = HashMap<String, String>()
                it.forEachIndexed { index, s ->  row[header[index]] = s}
                rows.add(row)
            }
            idx += 1
        }

        return rows
    }

    fun todayyyyyMMdd(): String{
        val df = SimpleDateFormat("yyyyMMdd")
        val today = Calendar.getInstance().time
        return df.format(today)
    }

    fun isoTimeStamp(): String {
        return LocalDateTime.now(ZoneId.of("GMT"))
            .format(DateTimeFormatter.ISO_DATE_TIME)
    }

    fun now(): String {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
    }

    fun toDateTimeFromhhmm(time: String): Date {
        val sdf = SimpleDateFormat("hhmm")
        val date = sdf.parse(time)
        return date
    }

    fun toMilliseconds(myDate: String) : Long {

        val localDateTime = LocalDateTime.parse(myDate,
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"))

        return localDateTime
                .atZone(ZoneId.of("UTC"))
                .toInstant().toEpochMilli()
    }

    fun toTime(nanoseconds: Long): Date{
        return Date(nanoseconds / 1000000)
    }

    fun toLocalTimeStamp(timestamp: String?, minusSeconds: Long = 0L): LocalDateTime? {
        return if(timestamp == null)
            null
        else {
            //val formatter = DateTimeFormatter.ISO_INSTANT
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val dateTime = LocalDateTime.parse(timestamp, formatter)
            dateTime.minusSeconds(minusSeconds)
        }
    }

    fun crossedTime(currentTime: Date, time: Date): Boolean = (currentTime.compareTo(time) >= 0)

    fun writeYaml(yamlData: Map<String, Any>, filename: String){
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.isPrettyFlow = true

        val yaml = Yaml(options)
        val writer = FileWriter(filename)

        yaml.dump(yamlData, writer)
        writer.close()
    }

    fun mapCopy(map: Map<String, Any>): HashMap<String, Any>{
        val newMap = HashMap<String, Any>()
        newMap.putAll(map)
        return newMap
    }

    fun toHexString(bytes: ByteArray) : String {
        val formatter = Formatter()
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        return formatter.toString()

    }

    fun hmacDigest(msg: String, keyString: String, algo: String): String {

        val key: SecretKey = SecretKeySpec(keyString.toByteArray(), algo)
        val mac: Mac = Mac.getInstance(algo)

        mac.init(key)

        val bytes = mac.doFinal(msg.toByteArray())

        return toHexString(bytes)

    }

    fun lcm(n1: Double, n2: Double) : Double {

        val scaleN1 = scale(n1)
        val scaleN2 = scale(n2)

        val s = max(scaleN1, scaleN2)

        val den = 10.0.pow(s).toLong()

        val numN1 = (n1 * den).toLong()
        val numN2 = (n2 * den).toLong()

        return lcm(numN1, numN2).toDouble() / den

    }

    fun scale(n1: Double) : Int {
        return BigDecimal(n1.toString()).scale()
    }

    fun lcm(n1: Long, n2: Long) : Long {
        return n1 * n2 / hcf(n1, n2)
    }

    fun hcf(n1: Long, n2: Long) : Long {

        var result = 1L

        var i = 1L
        while (i <= n1 && i <= n2) {

            // Checks if i is factor of both integers
            if (n1 % i == 0L && n2 % i == 0L)
                result = i

            ++i
        }

        return result

    }

    var rand: Random = Random()

    fun getRandom(min: Int, max: Int) : Int {
        return rand.nextInt((max - min) + 1) + min
    }
}