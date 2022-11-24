package trader

import constants.enums.ExchangeName
import constants.enums.OrderType
import constants.enums.Side
import models.Order
import java.util.*
import kotlin.collections.HashMap

object OrderTable {

    val orders = HashMap<String, Order>()

    private fun getId(): String {
        return UUID.randomUUID().toString()
    }

    fun new(symbol: String, exchange: ExchangeName, exchangeId: String, price: Double, amount: Double,
            side: Side, type: OrderType, postOnly: Boolean, reduceOnly: Boolean, ioc: Boolean) : Order {
        var order = Order(symbol, exchange, exchangeId, price, amount, side, type, postOnly, reduceOnly, ioc)
        add(order)
        return order
    }

    fun get(id: String): Order {
        return orders[id]!!
    }

    fun add(order: Order){
        orders[order.id] = order
    }

    fun has(id: String): Boolean {
        return (id in orders)
    }

    fun remove(id: String) : Order {
        return orders.remove(id)!!
    }

    fun reset() {
        orders.clear()
    }

}