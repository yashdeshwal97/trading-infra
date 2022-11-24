package models

import constants.enums.Side
import java.util.*

class OrderBookSide(val side: Side) {
    var levels: TreeMap<Double, OrderBookLevel>

    init {
        if(side == Side.BID) {
            levels = TreeMap(compareByDescending<Double>{it})
        } else {
            levels = TreeMap()
        }
    }

    fun addOrder(price: Double, amount: Double) {
        val level = if(price in levels) {
            levels[price]
        } else {
            levels[price] = OrderBookLevel(price, amount)
            levels[price]
        }

        levels[price]!!.amount += amount
        if (amount == 0.0) {
            levels.remove(price)
        }
    }

    fun delete(price: Double) {
        levels.remove(price)

    }
    fun reset(){
        levels.clear()
    }

    fun depth(): Int = levels.count()

    fun level(index: Int): OrderBookLevel? {
        var idx = 0
        for((_, v) in levels){
            if(idx == index){
                return v
            }
            idx++
        }
        return null
    }

}