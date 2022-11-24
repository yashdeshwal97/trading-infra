package services

import models.Order

interface IAuthExchange {
    fun sendNewOrder(order: Order): Boolean
    fun cancelOrder(order: Order): Boolean
    fun cancelAll(symbol: String): Boolean
    fun getPositions()
    fun getBalances()
}