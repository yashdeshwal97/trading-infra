package trader

import models.Order
import mu.KLogging

class TraderOrderBookLevel(val price: Double) {

    companion object: KLogging()

    val orders =  LinkedHashMap<String, Order>()

    var count = 0
        private set

    var amount = 0.0
        private set

    fun addOrder(order: Order) {
        orders[order.id] = order
        count += 1
        amount += order.openAmount

        logger.debug {"Amount $price $amount"}
    }

    fun removeOrder(order: Order){
        val order = orders.remove(order.id)
        count -= 1
        amount -= order!!.openAmount

        logger.debug {"Amount $price $amount"}

    }

    fun changeAmount(deltaAmount: Double) {
        amount -= deltaAmount

        logger.debug {"Amount $price $amount"}
    }

    fun ackedAmount(ackedWithoutConfirm: Boolean): Double {
        return (amount - unackedAmount(ackedWithoutConfirm))
    }

    fun unackedAmount(ackedWithoutConfirm: Boolean) : Double {
        var sum = 0.0
        orders.forEach { (_, order) ->
            if (!order.ioc && (ackedWithoutConfirm || order.exchangeId == null)) {
                sum += order.openAmount.toDouble()
            }
        }
        return sum
    }

    fun unackedCount(ackedWithoutConfirm: Boolean) : Int {
        var sum = 0
        orders.forEach { (_, order) ->
            if (!order.ioc && (ackedWithoutConfirm || order.exchangeId == null)) {
                sum += 1
            }
        }

        return sum
    }
}