package models.ftx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class FTXResult<T> {

    @JsonProperty("success")
    var isSuccess = false

    @JsonProperty("result")
    var result: T? = null

    override fun toString(): String {
        return "FTXResult{" +
                "success=" + isSuccess +
                ", result=" + result +
                '}'
    }
}
