package models.ftx.trade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class FTXOrder {
    @JsonProperty("createdAt")
    var createdAt: String? = null

    @JsonProperty("filledSize")
    var filledSize = 0.0

    @JsonProperty("future")
    var future: String? = null

    @JsonProperty("id")
    var id: Long = 0

    @JsonProperty("market")
    var market: String? = null

    @JsonProperty("price")
    var price = 0.0

    @JsonProperty("remainingSize")
    var remainingSize = 0.0

    @JsonProperty("side")
    var side: String? = null

    @JsonProperty("size")
    var size = 0.0

    @JsonProperty("status")
    var status: String? = null

    @JsonProperty("type")
    var type: String? = null

    @JsonProperty("reduceOnly")
    var isReduceOnly = false

    @JsonProperty("ioc")
    var isIoc = false

    @JsonProperty("postOnly")
    var isPostOnly = false

    @JsonProperty("clientId")
    var clientId: String? = null

    override fun toString(): String {
        return "FTXOrder{" +
                "createdAt='" + createdAt + '\'' +
                ", filledSize=" + filledSize +
                ", future='" + future + '\'' +
                ", id=" + id +
                ", market='" + market + '\'' +
                ", price=" + price +
                ", remainingSize=" + remainingSize +
                ", side='" + side + '\'' +
                ", size=" + size +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", reduceOnly=" + isReduceOnly +
                ", ioc=" + isIoc +
                ", postOnly=" + isPostOnly +
                ", clientId='" + clientId + '\'' +
                '}'
    }
}