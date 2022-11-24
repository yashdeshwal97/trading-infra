package services

import models.Order
import models.Position

interface IPrivateExchangeListener {
    fun onOrderResponse(order: Order)
    fun onPositionUpdate(position: Position)
    fun onWalletUpdate(position: Position)
    fun onCancelReject(orderIds: List<String>, exchangeId: String)
}