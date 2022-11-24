package services

import constants.enums.Side
import helpers.Helpers
import models.Contract
import models.OrderBook
import models.Snapshot

class OrderBookManager {

    val currencies: Set<Contract>
    val orderBooks = HashMap<String, OrderBook>()


    constructor(currencies: List<Contract>) {
        this.currencies = HashSet(currencies.map{it})
        currencies.forEach {
            orderBooks[it.symbol] = OrderBook(it)
        }
    }

    fun onSnapshot(snapShot: Snapshot, symbol: String) {
        val book = orderBooks[symbol]!!
        book.reset()
        updateOrderBook(book, snapShot)
    }

    fun onUpdates(snapShot: Snapshot, symbol: String) {
        val book = orderBooks[symbol]!!
        updateOrderBook(book, snapShot)
    }

    fun updateOrderBook(book: OrderBook, snapShot: Snapshot) {
            val bidList = snapShot.obSide[Side.BID]
            val askList = snapShot.obSide[Side.ASK]
            bidList!!.snap.forEach { bid ->
                val price = bid.price
                val amount = bid.amount

                if(Helpers.satoshiCompare(amount, 0.0) == 0) {
                    book.delete(Side.BID, price)
                } else {
                    book.addOrEdit(Side.BID, price, amount)
                }
            }

            askList!!.snap.forEach { ask ->
                val price = ask.price
                val amount = ask.amount

                if(Helpers.satoshiCompare(amount, 0.0) == 0) {
                    book.delete(Side.ASK, price)
                 } else {
                    book.addOrEdit(Side.ASK, price, amount)
                 }
            }
    }

}