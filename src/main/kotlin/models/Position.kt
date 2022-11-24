package models

import constants.enums.Side

@Suppress("UNUSED_PARAMETER")
class Position(symbol: String, exchangeId: String) {

    var amount = hashMapOf(Side.BID to 0.0, Side.ASK to 0.0)
    var price = hashMapOf(Side.BID to 0.0, Side.ASK to 0.0)
    var fee: Double = 0.0
    var exchangeId = exchangeId
    var symbol = symbol

    override fun toString(): String {
        return "Position(symbol=$symbol, amount=$amount, price=$price, commission=$fee)"
    }

}