package trader

import models.BestPrice
import constants.enums.Side
import mu.KotlinLogging
import services.ExchangeOverLord
import kotlin.math.abs
import kotlin.math.min

class TWAPTrader {
    val security: String
    val markets: List<Market>
    var targetPosition: Double = 0.0
    var intermediatePosition: Double = 0.0
    var perOrderSize: Double = 0.0
    var bestPrice = Side.values().associateWith { side -> BestPrice( null, 0.0) }.toMap()
    var interval: Long
    var lastOrderTime: Long = 0
    private val logger = KotlinLogging.logger {}

    constructor(security: String, interval: Long, perOrderSize: Double, markets: List<Market>) {
        this.security = security
        this.interval =  interval
        this.markets = markets
        this.perOrderSize = perOrderSize
        ExchangeOverLord.emptyQueueBind(::onEmptyQueue)
    }


    fun addPosition(size: Double, side: Side) {
        if (side == Side.BID) {
            targetPosition += size
        } else {
            targetPosition -= size
        }
    }

    fun onEmptyQueue(empty: Boolean) {
        updateBestPrice()
        val tPosition = targetPosition
        val now = System.currentTimeMillis()
        val positionLeft = tPosition - getCurrentPosition()
        if (positionLeft == 0.0)
        {
            return
        }
        val side = if (positionLeft > 0) Side.BID else Side.ASK
        val contract = bestPrice[side]!!.market!!.currency
        val market = bestPrice[side]!!.market
        val baseCurrencyBalance = TraderWalletManager.getBalance(market!!.exchangeID, market.currency.baseCurrency).netPosition()
        val quoteCurrencyBalance = TraderWalletManager.getBalance(market!!.exchangeID, market.currency.quoteCurrency).netPosition()
//        logger.info { "baseCurrency - ${market.currency.baseCurrency} : ${baseCurrencyBalance}" + " quoteCurrency- ${market.currency.quoteCurrency} : ${quoteCurrencyBalance}" }
        if (now - lastOrderTime > interval) {
            if (abs(positionLeft) >= contract.minOrderSize) {
                val orderSize = min(abs(positionLeft), perOrderSize)
                val price = if (side == Side.BID) {
                    bestPrice[Side.ASK]!!.price
                } else {
                    bestPrice[Side.BID]!!.price
                }
                if (side == Side.BID && quoteCurrencyBalance > (orderSize * price)) {
                    if (market!!.enqueueTakerOrder(side, price, orderSize)) {
                        lastOrderTime = now
                    } else {
                        lastOrderTime = 0
                    }
                }
                if (side == Side.ASK && baseCurrencyBalance >= orderSize) {
                    if (market!!.enqueueTakerOrder(side, price, orderSize)) {
                        lastOrderTime = now
                    } else {
                        lastOrderTime = 0
                    }
                }
                /*if (market!!.enqueueTakerOrder(side, price, orderSize)) {
                    lastOrderTime = now
                } else {
                    lastOrderTime = 0
                }*/
            }
        } else {
            bestPrice!![side]!!.market!!.enqueueCancel(side)
        }
        market!!.sendQueuedOrders()
    }

    fun updateBestPrice() {
        Side.values().forEach { side ->
            bestPrice[side]!!.market = null
            markets.forEach { market ->
                if(market.orderBook != null && market.orderBook!!.ready) {
                    if (bestPrice[side]!!.market == null) {
                        bestPrice[side]!!.market = market
                        bestPrice[side]!!.price = market.orderBook!!.price(side)
                    } else if (side.m * market.orderBook!!.price(side) < side.m * bestPrice[side]!!.price) {
                        bestPrice[side]!!.market = market
                        bestPrice[side]!!.price = market.orderBook!!.price(side)
                    }
                }
            }
        }
    }

    fun getCurrentPosition(): Double {
        var currentPosition = 0.0
        markets.forEach { market ->
            currentPosition += TraderPositionManager.getPosition(market.exchangeID, market.currency.symbol).netPosition()
        }
        return currentPosition
    }



}