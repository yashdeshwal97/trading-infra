package trader

import constants.enums.Side

class TraderPosition {
    val exchangeId: String
    val coin: String

    val amount = HashMap(Side.values().associateWith { 0.0 })
    val price = HashMap(Side.values().associateWith { 0.0 })

    constructor(exchangeId: String, coinName: String) {
        this.exchangeId = exchangeId
        this.coin = coinName
    }

    fun onTrade(side: Side, amount: Double, price: Double) {
        val totalAmount = (this.amount[side]!! + amount)
        this.price[side] = (this.price[side]!! * this.amount[side]!! + price * amount) / totalAmount
        this.amount[side] = totalAmount
    }

    fun netPosition(): Double {
        return amount[Side.BID]!! - amount[Side.ASK]!!
    }

    fun onWalletUpdate(amount: Double) {
        this.amount[Side.BID] = amount
    }

}