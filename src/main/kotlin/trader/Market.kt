package trader

import constants.enums.OrderStatus
import constants.enums.OrderType
import constants.enums.Side
import helpers.Helpers
import models.Contract
import models.Order
import models.OrderBook
import models.Position
import mu.KLogging
import services.ExchangeOverLord
import services.IPrivateExchangeListener
import services.IPublicExchangeListener
import services.OrderManager
import java.util.*

open class Market : IPrivateExchangeListener, IPublicExchangeListener {

    companion object: KLogging()

    val exchangeID: String
    val currency: Contract

    var orderBook: OrderBook? = null
        private set

    protected val traderOrderBook = TraderOrderBook()

    protected val newOrdersToSend = ArrayList<Order>()
    protected val cancelOrdersToSend = ArrayList<Order>()

    protected val maxOpenOrders: Int
    protected val maxPerLevelOpenOrders: Int

    var newOrderRejectCount = 0
        private set

    var noResponseInLongTime: Boolean = false
        private set

    open var ackedWithoutConfirm = false

    constructor(exchangeID: String, currency: Contract,
                maxOpenOrders: Int, maxPerLevelOpenOrders: Int) {

        this.exchangeID = exchangeID
        this.currency = currency

        this.maxOpenOrders = maxOpenOrders
        this.maxPerLevelOpenOrders = maxPerLevelOpenOrders
    }

    fun setup() {
        ExchangeOverLord.bind(currency.exchange, currency.symbol, this)
        ExchangeOverLord.bindPrivate(exchangeID, currency.symbol, this)
    }

    fun enqueueTakerOrder(side: Side, price: Double, amount: Double): Boolean {

        val orderBookSide = traderOrderBook.obSide[side]!!
        val openOrderCount = orderBookSide.count

        if (openOrderCount < maxOpenOrders) {
            val order = OrderTable.new(
                currency.symbol, currency.exchange, exchangeID, price, amount, side,
                OrderType.LIMIT, postOnly = false, reduceOnly = false, ioc = true
            )
            enqueueNewOrder(order)
            return true
        } else {
            logger.info{"Taker Failed => Symbol:${this.currency.symbol} | side:$side | " +
                    "price:$price | amount:$amount | OpenOrderCount: $openOrderCount "
            }
        }

        return false
    }

    fun getTakerQuantity(side: Side, price: Double): Double {
        val m = side.m

        val obSideLevels = traderOrderBook.obSide[side]!!.levels
        var takerQty = 0.0

        for ((t, u) in obSideLevels) {
            if(m * t >= m * price) {
                takerQty += u.amount
            } else {
                break
            }
        }

        return takerQty

    }

    fun getAmount(side: Side): Double {
        return traderOrderBook.obSide[side]!!.amount
    }

    fun enqueueLadderTop(
        side: Side,
        startPrice: Double,
        maxLadderLevels: Int,
        amount: Double,
        maxOrderCountAllowed: Int = 10,
        newOrderAllowed: Boolean = true,
        lastPrice: Double? = null,
        maxAmountAllowed: Double = Double.MAX_VALUE
    ) : Boolean {
        var startNewEnqueueCount = newOrdersToSend.count()
        var startCancelEnqueueCount = cancelOrdersToSend.count()

        val m = side.m

        val orderBookSide = traderOrderBook.obSide[side]!!
        val orderBookLevels = orderBookSide.levels.values.toList()
        val orderBookLevelKeys = orderBookSide.levels.keys.toList()
        var openOrderCount = orderBookSide.count

        val maxOpenOrdersAllowed = minOf(maxOpenOrders, maxOrderCountAllowed)

        val unackedAmount = unackedAmount(side)
        var amountToLadder = minOf(maxAmountAllowed, minOf(maxLadderLevels, maxOpenOrdersAllowed) * amount) -
                unackedAmount

        var level = 0
        val levelCount = orderBookLevels.count()
        var sendNewOrder = true

        var hasOrderAtPrice = false

        // cancel orders above startPrice

        while(level < levelCount) {
            val price = orderBookLevelKeys[level]
            val orderBookLevel = orderBookLevels[level]
            val comparison = (m * price - m * startPrice)

            if(comparison > 0.0) {
                enqueueLevelCancel(orderBookLevel)
                sendNewOrder = false
                amountToLadder -= (orderBookLevel.ackedAmount(ackedWithoutConfirm))
                level += 1

            } else if(comparison == 0.0) {

                if(orderBookLevel.amount > amount) {
                    enqueueLevelCancel(orderBookLevel)
                }

                amountToLadder -= (orderBookLevel.ackedAmount(ackedWithoutConfirm))
                hasOrderAtPrice = true
                sendNewOrder = false
                level += 1

                break
            } else {
                break
            }
        }

        if(newOrderAllowed) {
            if (sendNewOrder && amountToLadder >= amount &&
                openOrderCount < maxOpenOrdersAllowed) {

                val order = OrderTable.new(
                    currency.symbol, currency.exchange, exchangeID, startPrice, amount, side,
                    OrderType.LIMIT, postOnly = true, reduceOnly = false, ioc = false
                )
                enqueueNewOrder(order)

            }

            // this is being done to make space for future orders at this price
            // in case we were not able to send an order in this iteration
            if (!hasOrderAtPrice) {
                amountToLadder -= amount
            }
        }

        while(level < levelCount) {
            val orderBookLevel = orderBookLevels[level]
            val ackedAmount = orderBookLevel.ackedAmount(ackedWithoutConfirm)
            val price = orderBookLevelKeys[level]

            if(Helpers.satoshiCompare(amountToLadder, ackedAmount) < 0
                || (lastPrice != null && m * price < m * lastPrice)) {
                enqueueLevelCancel(orderBookLevel)
            }

            amountToLadder -= ackedAmount
            level += 1
        }

        return (newOrdersToSend.count() > startNewEnqueueCount || cancelOrdersToSend.count() > startCancelEnqueueCount)
    }

    open fun enqueueLevelCancel(level: TraderOrderBookLevel) : Boolean {
        var allCancelled = true

        level.orders.forEach { (_, u) ->
            if(!enqueueCancel(u)) {
                allCancelled = false
            }
        }

        return allCancelled

    }

    open fun unackedAmount(side: Side) : Double {
        var sum = 0.0
        val orderBookSide = traderOrderBook.obSide[side]!!
        val orderBookLevels = orderBookSide.levels.values

        orderBookLevels.forEach { level ->
            sum += level.unackedAmount(ackedWithoutConfirm)
        }

        return sum
    }

    open fun unackedCount(side: Side) : Int {
        var sum = 0
        val orderBookSide = traderOrderBook.obSide[side]!!
        val orderBookLevels = orderBookSide.levels.values

        orderBookLevels.forEach { level ->
            sum += level.unackedCount(ackedWithoutConfirm)
        }

        return sum
    }


    open fun enqueueNewOrder(order: Order) {
        newOrdersToSend.add(order)
    }

    open fun clearNewOrder() {
        newOrdersToSend.clear()
    }

    open fun clearNewOrdersSide(side: Side) {
        newOrdersToSend.removeIf { order ->
            order.side == side
        }
    }

    open fun enqueueCancel(order: Order) : Boolean {
        if(canCancel(order)) {
            cancelOrdersToSend.add(order)
            return true
        }

        return order.cancelSent
    }

    open fun canCancel(side: Side, price: Double) : Boolean {
        val orders = traderOrderBook.obSide[side]!!.levels[price]!!.orders
        var canCancelAll = true

        orders.forEach { (t, u) ->
            if(!canCancel(u)) {
                canCancelAll = false
            }
        }

        return canCancelAll

    }

    open fun canCancel(order: Order) : Boolean {
        return order.exchangeId != null && !order.cancelSent && order !in cancelOrdersToSend
    }

    override fun onOrderBookUpdation(book: OrderBook) {
        this.orderBook = book
    }

    override fun onOrderResponse(response: Order) {
        if(OrderTable.has(response.id)) {

            val order = OrderTable.get(response.id)

            if (order.exchangeId == exchangeID && order.symbol == currency.symbol) {

                logger.info { "Response => $response" }

                val oldTradedAmount = order.filledAmount
                val oldTradedPrice = order.filledPrice
                val newTradedAmount = response.filledAmount
                val newTradedPrice = response.filledPrice
                val tradeAmount = newTradedAmount - oldTradedAmount

                if (tradeAmount > 0.0) {
                    logger.info {
                        "Trade => Symbol: ${currency.symbol} | Side:${response.type} | InternalOrderID: ${response.id} | " +
                                "OldAmount:${oldTradedAmount} | OldPrice:${oldTradedPrice} | NewAmount:${newTradedAmount} | " +
                                "NewPrice:${newTradedPrice} | Amount:${tradeAmount}"
                    }


                    val position = TraderPositionManager.getPosition(exchangeID, currency.symbol)
                    val tradePrice = (newTradedAmount * newTradedPrice - oldTradedAmount * oldTradedPrice) / tradeAmount

                    position.onTrade(response.side, tradeAmount, tradePrice)

                    val baseCurrencyBalance = TraderWalletManager.getBalance(exchangeID, currency.baseCurrency)
                    val quoteCurrencyBalance = TraderWalletManager.getBalance(exchangeID, currency.quoteCurrency)
                    if (order.side == Side.BID) {
                        baseCurrencyBalance.onWalletUpdate(baseCurrencyBalance.amount[Side.BID]!! + tradeAmount)
                        quoteCurrencyBalance.onWalletUpdate(quoteCurrencyBalance.amount[Side.BID]!! - (tradeAmount * newTradedPrice))
                    } else {
                        baseCurrencyBalance.onWalletUpdate(baseCurrencyBalance.amount[Side.BID]!! - tradeAmount)
                        quoteCurrencyBalance.onWalletUpdate(quoteCurrencyBalance.amount[Side.BID]!! + (tradeAmount * newTradedPrice))
                    }
                    TraderWalletManager.positions.forEach { id, pos ->
                        pos.forEach { coin, balance ->
                            logger.info { "Coin: ${balance.coin}  Balance: ${balance.amount[Side.BID]}" }
                        }
                    }
                }

                //TODO: handle fee paid

                order.filledAmount = response.filledAmount
                order.filledPrice = response.filledPrice

                order.status = response.status

                if (order.isClosed) {
                    OrderTable.remove(order.id)
                }

                if(order.status == OrderStatus.REJECTED) {
                    newOrderRejectCount++
                } else {
                    newOrderRejectCount = 0
                }
            }
        }

    }

    override fun onCancelReject(orderIds: List<String>, exchangeId: String) {
        orderIds.forEach { id ->
            if(OrderTable.has(id)) {
                val order = OrderTable.get(id)
                if (order.exchangeId == exchangeID && order.symbol == currency.symbol) {
                    order.cancelSent = false
                }

            }
        }
    }


    override fun onPositionUpdate(position: Position) {

    }

    override fun onWalletUpdate(position: Position) {
        val tposition = TraderWalletManager.getBalance(position.exchangeId, position.symbol)
        tposition.onWalletUpdate(position.amount[Side.BID]!!)
    }

    open fun enqueueCancel() : Boolean {
        val b = enqueueCancel(Side.BID)
        val a = enqueueCancel(Side.ASK)

        return a || b
    }

    open fun enqueueCancel(side: Side) : Boolean {
        var startCancelEnqueueCount = cancelOrdersToSend.count()
        val orderBookSide = traderOrderBook.obSide[side]!!
        val orderBookLevels = orderBookSide.levels.values

        return if(orderBookLevels.isNotEmpty()) {
            orderBookLevels.forEach {
                enqueueLevelCancel(it)
            }
            (cancelOrdersToSend.count() > startCancelEnqueueCount)

        } else {
            false
        }
    }

    open fun enqueueCancelBelowPrice(side: Side, price: Double) : Boolean {

        val m = side.m

        var startCancelEnqueueCount = cancelOrdersToSend.count()
        val orderBookSide = traderOrderBook.obSide[side]!!
        val orderBookLevels = orderBookSide.levels.values

        return if(orderBookLevels.isNotEmpty()) {
            orderBookLevels.forEach {
                if(m * it.price < m * price) {
                    enqueueLevelCancel(it)
                }
            }

            (cancelOrdersToSend.count() > startCancelEnqueueCount)

        } else {
            false
        }
    }

    open fun enqueueCancelAbovePrice(side: Side, price: Double) : Boolean {

        val m = side.m

        var startCancelEnqueueCount = cancelOrdersToSend.count()
        val orderBookSide = traderOrderBook.obSide[side]!!
        val orderBookLevels = orderBookSide.levels.values

        return if(orderBookLevels.isNotEmpty()) {
            orderBookLevels.forEach {
                if(m * it.price > m * price) {
                    enqueueLevelCancel(it)
                }
            }

            (cancelOrdersToSend.count() > startCancelEnqueueCount)

        } else {
            false
        }
    }

    fun orderPriceBetweenPrice(side: Side, price1: Double, price2: Double) : Double {
        return traderOrderBook.orderPriceBetweenPrice(side, price1, price2)
    }

    fun getTopPrice(side: Side): Double {
        return traderOrderBook.getTopPrice(side)
    }

    fun getTopAmount(side: Side): Double {
        return traderOrderBook.getTopAmount(side)
    }

    fun getTopAckedAmount(side: Side): Double {
        return traderOrderBook.getTopAckedAmount(side, ackedWithoutConfirm)
    }

    fun getTopUnackedAmount(side: Side): Double {
        return traderOrderBook.getTopUnackedAmount(side, ackedWithoutConfirm)
    }

    fun getBottomPrice(side: Side): Double {
        return traderOrderBook.getBottomPrice(side)
    }

    fun hasOrders(): Boolean {
        return hasOrders(Side.ASK) || hasOrders(Side.BID)
    }

    fun hasTakerOrders(): Boolean {
        return hasTakerOrders(Side.ASK) || hasTakerOrders(Side.BID)
    }

    fun hasTakerOrders(side: Side) :  Boolean {
        return traderOrderBook.hasTakerOrders(side)
    }

    fun hasOrders(side: Side): Boolean {
        return traderOrderBook.hasOrders(side)
    }

    fun getOpenOrders() : List<Order> {
        return traderOrderBook.getOrders()
    }

    fun getOpenOrderCount(side: Side): Int {
        return traderOrderBook.getOpenOrderCount(side)
    }

    fun getOpenAmount(side: Side): Double {
        return traderOrderBook.getOpenAmount(side)
    }

    open fun reset() {
        traderOrderBook.reset()
        newOrderRejectCount = 0
        noResponseInLongTime = false
    }

    fun sendQueuedOrders() : Boolean {
        val a = OrderManager.sendNewOrder(newOrdersToSend)
        val b = OrderManager.cancelOrder(cancelOrdersToSend)

        newOrdersToSend.clear()

        cancelOrdersToSend.forEach { order ->
            order.cancelSent = true
        }

        cancelOrdersToSend.clear()

        return a || b

    }

}