package models.ftx.account

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class FTXAccount {
    @JsonProperty("backstopProvider")
    var isBackstopProvider = false

    @JsonProperty("collateral")
    var collateral = 0.0

    @JsonProperty("freeCollateral")
    var freeCollateral = 0.0

    @JsonProperty("initialMarginRequirement")
    var initialMarginRequirement = 0.0

    @JsonProperty("leverage")
    var leverage = 0.0

    @JsonProperty("liquidating")
    var isLiquidating = false

    @JsonProperty("maintenanceMarginRequirement")
    var maintenanceMarginRequirement = 0.0

    @JsonProperty("makerFee")
    var makerFee = 0.0

    @JsonProperty("marginFraction")
    var marginFraction = 0.0

    @JsonProperty("openMarginFraction")
    var openMarginFraction = 0.0

    @JsonProperty("takerFee")
    var takerFee = 0.0

    @JsonProperty("totalAccountValue")
    var totalAccountValue = 0.0

    @JsonProperty("totalPositionSize")
    var totalPositionSize = 0.0

    @JsonProperty("username")
    var username: String? = null

    @JsonProperty("positions")
    var positions: List<FTXPosition>? = null

    override fun toString(): String {
        return "FTXAccount{" +
                "backstopProvider=" + isBackstopProvider +
                ", collateral=" + collateral +
                ", freeCollateral=" + freeCollateral +
                ", initialMarginRequirement=" + initialMarginRequirement +
                ", leverage=" + leverage +
                ", liquidating=" + isLiquidating +
                ", maintenanceMarginRequirement=" + maintenanceMarginRequirement +
                ", makerFee=" + makerFee +
                ", marginFraction=" + marginFraction +
                ", openMarginFraction=" + openMarginFraction +
                ", takerFee=" + takerFee +
                ", totalAccountValue=" + totalAccountValue +
                ", totalPositionSize=" + totalPositionSize +
                ", username='" + username + '\'' +
                ", positions=" + positions +
                '}'
    }
}
