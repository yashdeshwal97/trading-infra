package models.ftx.requests

import com.fasterxml.jackson.annotation.JsonProperty

class FTXCancelRequest(@field:JsonProperty("market") var market: String?) {

    override fun toString(): String {
        return "FTXCancelRequest{" +
                "market='" + market + '\'' +
                '}'
    }
}
