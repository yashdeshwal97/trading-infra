package services

import models.Order
import java.util.concurrent.ConcurrentHashMap

abstract class OrderStateManager {
    var sentOrders: ConcurrentHashMap<String, Order> = ConcurrentHashMap()

    fun addNewOrder(order: Order) {
        sentOrders[order.id] = order
    }

    fun deleteOrder(id: String) {
        if (sentOrders.containsKey(id)) {
            sentOrders.remove(id)
        }
    }
}