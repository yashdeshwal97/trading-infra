package models.ftx.requests

import com.fasterxml.jackson.annotation.JsonProperty

class FTXOrderRequest(@field:JsonProperty("market") var market: String,
                      @field:JsonProperty("side") var side: String,
                      @field:JsonProperty("price") var price: Double,
                      @field:JsonProperty("type") var type: String,
                      @field:JsonProperty("size") var size: Double,
                      @field:JsonProperty("reduceOnly") var isReduceOnly: Boolean,
                      @field:JsonProperty("ioc") var isIoc: Boolean,
                      @field:JsonProperty("postOnly") var isPostOnly: Boolean,
                      @field:JsonProperty("clientId") var clientId: String) {

    override fun toString(): String {
        return "FTXOrderRequest{" +
                "market='" + market + '\'' +
                ", side='" + side + '\'' +
                ", price=" + price +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", reduceOnly=" + isReduceOnly +
                ", ioc=" + isIoc +
                ", postOnly=" + isPostOnly +
                ", clientId='" + clientId + '\'' +
                '}'
    }
}