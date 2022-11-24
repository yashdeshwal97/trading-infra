package trader

import constants.enums.Side
import models.Order
import mu.KLogging
import java.util.*

class TraderOrderBookSide {

    companion object: KLogging()

    private val defaultPrice: Double

    val side: Side

    val levels: TreeMap<Double, TraderOrderBookLevel>

    var amount: Double = 0.0
        private set

    var count: Int = 0
        private set

    constructor(side: Side) {
        this.side = side

        if(side == Side.BID) {
            levels = TreeMap(compareByDescending<Double>{it})
            defaultPrice = -Double.MAX_VALUE
        } else {
            levels = TreeMap()
            defaultPrice = Double.MAX_VALUE
        }
    }

    fun orderPriceBetweenPrice(price1: Double, price2: Double): Double {
        val p1 = minOf(price1, price2)
        val p2 = maxOf(price1, price2)
        for((k,_) in levels) {
            if(k in p1..p2){
                return k
            }
        }

        return Double.NaN
    }

    fun getTopPrice(): Double {
        return if(levels.isNotEmpty()) {
            levels.firstKey()
        } else {
            defaultPrice
        }
    }

    fun getTopAmount(): Double {
        return if(levels.isNotEmpty()) {
            levels.firstEntry().value.amount
        } else {
            0.0
        }
    }


    fun getTopAckedAmount(ackedWithoutConfirm: Boolean): Double {
        return if(levels.isNotEmpty()) {
            levels.firstEntry().value.ackedAmount(ackedWithoutConfirm)
        } else {
            0.0
        }
    }

    fun getTopUnackedAmount(ackedWithoutConfirm: Boolean): Double {
        return if(levels.isNotEmpty()) {
            levels.firstEntry().value.unackedAmount(ackedWithoutConfirm)
        } else {
            0.0
        }
    }

    fun getBottomPrice(): Double {
        var last = defaultPrice

        for((k,_) in levels) {
            last = k
        }

        return last
    }

    fun addOrder(order: Order) {
        val price = order.price
        val level = if(price in levels) {
            levels[price]
        } else {
            levels[price] = TraderOrderBookLevel(price)
            levels[price]
        }

        level!!.addOrder(order)
        count += 1
        amount += order.openAmount
        logger.debug {"OrderBookSide Add: $side $count $amount"}


    }

    fun removeOrder(order: Order) {
        val price = order.price
        levels[price]!!.removeOrder(order)

        if(levels[price]!!.count == 0){
            levels.remove(price)
        }

        amount -= order.openAmount
        count -= 1
        logger.debug {"OrderBookSide Remove: $side $count $amount"}
    }

    fun onOrderResponse(response: Order) {
        val price = response.price.toDouble()
        val id = response.id
        val level = levels[price]!!

        val order = level.orders[id]!!

        if(!response.isOpen) {
            removeOrder(order)
        } else if(response.filledAmount > order.filledAmount) {
            val delta = response.filledAmount - order.filledAmount
            level.changeAmount(delta)
            amount -= delta
        }
    }

    // TODO: stop using ioc to determine if it was a taker order
    fun hasTakerOrders() : Boolean  {
        for ((d, strategyOrderBookLevel) in levels) {
            for ((l, strategyOrder) in strategyOrderBookLevel.orders) {
                if(strategyOrder.ioc) {
                    return true
                }
            }
        }

        return false
    }

    fun hasOrders() = count > 0

    fun getOrders(): List<Order> {
        val orders = ArrayList<Order>()

        levels.forEach { (_, u) ->
            orders.addAll(u.orders.values)
        }

        return orders
    }

    fun reset() {
        levels.clear()
        count = 0
        amount = 0.0
    }
}