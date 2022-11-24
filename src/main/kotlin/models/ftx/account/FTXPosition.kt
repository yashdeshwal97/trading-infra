package models.ftx.account

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class FTXPosition {
    @JsonProperty("cost")
    var cost = 0.0

    @JsonProperty("cumulativeBuySize")
    var cumulativeBuySize = 0.0

    @JsonProperty("cumulativeSellSize")
    var cumulativeSellSize = 0.0

    @JsonProperty("entryPrice")
    var entryPrice = 0.0

    @JsonProperty("estimatedLiquidationPrice")
    var estimatedLiquidationPrice = 0.0

    @JsonProperty("future")
    var future: String? = null

    @JsonProperty("initialMarginRequirement")
    var initialMarginRequirement = 0.0

    @JsonProperty("longOrderSize")
    var longOrderSize = 0.0

    @JsonProperty("maintenanceMarginRequirement")
    var maintenanceMarginRequirement = 0.0

    @JsonProperty("netSize")
    var netSize = 0.0

    @JsonProperty("openSize")
    var openSize = 0.0

    @JsonProperty("realizedPnl")
    var realizedPnl = 0.0

    @JsonProperty("shortOrderSize")
    var shortOrderSize = 0.0

    @JsonProperty("side")
    var side: String? = null

    @JsonProperty("size")
    var size = 0.0

    @JsonProperty("unrealizedPnl")
    var unrealizedPnl = 0.0

    override fun toString(): String {
        return "FTXPosition{" +
                "cost=" + cost +
                "cumulativeBuySize" + cumulativeBuySize +
                "cumulativeSellSize" + cumulativeSellSize +
                ", entryPrice=" + entryPrice +
                ", estimatedLiquidationPrice=" + estimatedLiquidationPrice +
                ", future='" + future + '\'' +
                ", initialMarginRequirement=" + initialMarginRequirement +
                ", longOrderSize=" + longOrderSize +
                ", maintenanceMarginRequirement=" + maintenanceMarginRequirement +
                ", netSize=" + netSize +
                ", openSize=" + openSize +
                ", realizedPnl=" + realizedPnl +
                ", shortOrderSize=" + shortOrderSize +
                ", side='" + side + '\'' +
                ", size=" + size +
                ", unrealizedPnl=" + unrealizedPnl +
                '}'
    }
}
