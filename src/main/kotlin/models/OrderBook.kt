package models

import constants.enums.Side

class OrderBook {

    private val obSide = Side.values().associateWith { side -> OrderBookSide(side) }.toMap()

    val contract: Contract

    constructor(contract: Contract) {
        this.contract = contract
    }

    fun price(side: Side) : Double {
        return level(side,0)!!.price
    }

    fun amount(side: Side) : Double {
        return level(side,0)!!.amount
    }

    private fun depth(side: Side): Int {
        return obSide[side]!!.depth()
    }

    val mid: Double
        get() {
            return (price(Side.BID) + price(Side.ASK)) / 2.0
        }

    val depth: Int
        get() {
            return minOf(this.depth(Side.BID), this.depth(Side.ASK))
        }

    val spread: Double
        get() {
            return price(Side.ASK) - price(Side.BID)
        }

    val spreadPct: Double
        get() {
            return spread / mid * 100.0
        }

    fun delete(side: Side, price: Double) {
        obSide[side]!!.delete(price)
    }

    fun addOrEdit(side: Side, price: Double, amount: Double) : Unit {
        obSide[side]!!.addOrder(price, amount)
    }

    fun reset(){
        for((_, o) in obSide){
            o.reset()
        }
    }

    private fun level(side: Side, index: Int): OrderBookLevel? {
        return obSide[side]!!.level(index)
    }

    fun copy(): OrderBook {
        val orderBook = OrderBook(this.contract)

        listOf(Side.BID, Side.ASK).forEach {
            val fromSide = this.obSide[it]!!
            val toSide = orderBook.obSide[it]!!
            for((price, orderBookLevel) in fromSide.levels) {
                var level = OrderBookLevel(orderBookLevel.price, orderBookLevel.amount)
                toSide.levels[price] = level
            }
        }

        return orderBook
    }

    val ready: Boolean
        get() {
            return this.depth > 0 && this.price(Side.BID) < this.price(Side.ASK)
        }

    fun toString(levels: Int): String {
        var str = StringBuilder("")

        str.append("Exchange=${contract.exchange} | " +
                   "Symbol=${contract.symbol}\n")

        var num = minOf(levels, depth(Side.BID), depth(Side.ASK))

        for(i in 0 until num){
            val bidLevel = level(Side.BID, i) as OrderBookLevel
            val askLevel = level(Side.ASK, i) as OrderBookLevel

            val format = if(i < num - 1) {
                "%16.8f %18.8f | %-20.8f %-16.8f\n"
            } else {
                "%16.8f %18.8f | %-20.8f %-16.8f"
            }

            str.append(format.format(bidLevel.amount, bidLevel.price,
                askLevel.price, askLevel.amount))
        }

        return str.toString()
    }
}