package models

import constants.enums.*
import helpers.Helpers

class Contract {
    var symbol: String
    val exchange: ExchangeName
    val options: Map<String, Any>?
    val tickSize: Double
    val minOrderSize: Double
    val maxOrderSize: Double
    val lotSize: Double
    val contractSize: Double
    val isLinear: Boolean
    val type: ContractType
    val baseCurrency: String
    val quoteCurrency: String
    val marginCurrency: String

    constructor(properties: Map<String, Any>, exchangeName: String) {
        options = properties
        symbol = options["symbol"] as String
        exchange = ExchangeName.valueOf(exchangeName)
        tickSize = (options["tickSize"] as Number).toDouble()
        minOrderSize = (options["minOrderSize"] as Number).toDouble()
        lotSize = (options["lotSize"] as Number).toDouble()
        maxOrderSize = if ("maxOrderSize" in options) (options["maxOrderSize"] as Number).toDouble() else Double.MAX_VALUE
        contractSize = (options["contractSize"] as Number).toDouble()
        isLinear = options["isLinear"] as Boolean
        type = ContractType.valueOf(options["type"] as String)
        baseCurrency = options["base"] as String
        quoteCurrency = options["quote"] as String
        marginCurrency = options["margin"] as String
    }


    fun toRealAmount(contracts: Double, price: Double) : Double {
        val contractValue = if(isLinear) {
            contractSize * price
        } else {
            contractSize
        }

        return contracts * contractValue / price
    }

    fun toContracts(amount: Double, price: Double) : Double {
        val contractValue = if(isLinear) {
            contractSize * price
        } else {
            contractSize
        }

        return Helpers.floorToBase(amount  * price / contractValue, lotSize)
    }

}