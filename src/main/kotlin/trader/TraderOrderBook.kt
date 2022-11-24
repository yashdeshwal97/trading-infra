package trader

import constants.enums.Side
import models.Order

class TraderOrderBook {

    val obSide = Side.values().associateWith { side -> TraderOrderBookSide(side) }

    fun addOrder(order: Order) {
        obSide[order.side]!!.addOrder(order)
    }

    fun removeOrder(order: Order) {
        obSide[order.side]!!.removeOrder(order)
    }

    fun onOrderResponse(order_response: Order) {
        obSide[order_response.side]!!.onOrderResponse(order_response)
    }

    fun orderPriceBetweenPrice(side: Side, price1: Double, price2: Double): Double {
        return obSide[side]!!.orderPriceBetweenPrice(price1, price2)
    }

    fun getTopPrice(side: Side): Double {
        return obSide[side]!!.getTopPrice()
    }

    fun getTopAmount(side: Side): Double {
        return obSide[side]!!.getTopAmount()
    }

    fun getTopAckedAmount(side: Side, ackedWithoutConfirm: Boolean): Double {
        return obSide[side]!!.getTopAckedAmount(ackedWithoutConfirm)
    }

    fun getTopUnackedAmount(side: Side, ackedWithoutConfirm: Boolean): Double {
        return obSide[side]!!.getTopUnackedAmount(ackedWithoutConfirm)
    }

    fun getBottomPrice(side: Side): Double {
        return obSide[side]!!.getBottomPrice()
    }

    fun hasTakerOrders(side: Side): Boolean {
        return obSide[side]!!.hasTakerOrders()
    }

    fun hasOrders(side: Side): Boolean {
        return obSide[side]!!.hasOrders()
    }

    fun getOpenOrderCount(side: Side): Int {
        return obSide[side]!!.count
    }

    fun getOpenAmount(side: Side): Double {
        return obSide[side]!!.amount
    }

    fun getOrders() : List<Order> {
        val orders = ArrayList<Order>()
        orders.addAll(obSide[Side.BID]!!.getOrders())
        orders.addAll(obSide[Side.ASK]!!.getOrders())
        return orders
    }

    fun getOrders(side: Side) : List<Order> {
        return obSide[side]!!.getOrders()
    }


    fun reset() {
        obSide[Side.BID]!!.reset()
        obSide[Side.ASK]!!.reset()
    }
}
