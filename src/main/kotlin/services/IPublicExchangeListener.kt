package services

import models.OrderBook

interface IPublicExchangeListener {
    fun onOrderBookUpdation(book: OrderBook)
}