package services.ftx

import constants.enums.OrderStatus
import constants.enums.Side
import models.ftx.account.FTXPosition
import models.Order
import models.Position
import models.ftx.trade.FTXOrder
import models.ftx.FTXResult
import services.OrderStateManager

class FTXOrderStateManager : OrderStateManager() {

    fun receiveNewOrderUpdate(response: FTXResult<FTXOrder>): Order? {
        var order: Order?
        if (sentOrders.containsKey(response.result!!.clientId)) {
            order = sentOrders.get(response.result!!.clientId)
        } else {
            return null
        }
        if (response.isSuccess) {
            val orderId = response!!.result!!.id
            order!!.exchangeOrderId = orderId.toString()
            order.filledAmount = response!!.result!!.filledSize
            order.filledPrice = response!!.result!!.price
            val remainingSize = response!!.result!!.remainingSize
            var status: String? = response!!.result!!.status
            order.status = if (status.equals("new") || status.equals("open")) {
                OrderStatus.OPEN
            } else if (status.equals("closed") && remainingSize == 0.0) {
                OrderStatus.CLOSED
            } else {
                OrderStatus.REJECTED
            }
        } else {
            OrderStatus.REJECTED
        }
        sentOrders[order!!.id] = order
        val dupOrder = order.duplicate()
        if (order.status == OrderStatus.CLOSED || order.status == OrderStatus.REJECTED) {
            deleteOrder(order.id)
        }
        return dupOrder
    }

    fun receiveCancelOrderUpdate(response: FTXResult<String>, id: String): Order? {
        var order: Order?
        if (sentOrders.containsKey(id)) {
            order = sentOrders.get(id)
        } else {
            return null
        }
        order!!.isCancelled = response.isSuccess
        sentOrders[order.id] = order
        return order.duplicate()
    }

    fun receiveOrderUpdate(data: Map<*, *>): Order? {
        var order: Order?
        if (sentOrders.containsKey(data["clientId"] as String)) {
            order = sentOrders.get(data["clientId"] as String)
        } else {
            return null
        }
        val orderId = data["id"] as Long
        order!!.exchangeOrderId = orderId.toString()
        order.filledAmount = data["filledSize"] as Double
        order.filledPrice = if (data["avgFillPrice"] != null) {
            data["avgFillPrice"] as Double
        } else {
            0.0
        }
        val remainingSize = data["remainingSize"] as Double
        var status: String = data["status"] as String
        order.status = if (status.equals("new") || status.equals("open")) {
            OrderStatus.OPEN
        } else if (status.equals("closed") && remainingSize == 0.0) {
            OrderStatus.CLOSED
        } else {
            order.status
        }
        sentOrders[order.id] = order
        val dupOrder = order.duplicate()
        if (order.status == OrderStatus.CLOSED) {
            deleteOrder(order.id)
        }
        return dupOrder
    }

    fun receivePositionUpdate(response: FTXResult<List<FTXPosition>>, exchangeId: String): List<Position>? {
        if(response.result == null) {
            return null
        }
        var positions = mutableListOf<Position>()
        response.result!!.forEach {ftxPosition ->
            val position = Position(ftxPosition.future.toString(), exchangeId)
            val side = if (ftxPosition.side == "sell") {
                Side.ASK
            } else {
                Side.BID
            }
            position.price[side] = ftxPosition.entryPrice
            position.amount[Side.ASK] = ftxPosition.cumulativeSellSize
            position.amount[Side.BID] = ftxPosition.cumulativeBuySize
            position.fee = ftxPosition.cost
            positions.add(position)
        }
        return positions
    }

    fun receiveWalletUpdate(response: FTXResult<List<Map<String, Any>>>, exchangeId: String): List<Position>? {
        if(response.result == null) {
            return null
        }
        var positions = mutableListOf<Position>()
        response.result!!.forEach {
            val position = Position(it["coin"] as String, exchangeId)
            val total = it["total"] as Double
            if (total > 0.0) {
                position.amount[Side.BID] = total
            } else {
                position.amount[Side.ASK] = total
            }
            positions.add(position)
        }
        return positions
    }
}