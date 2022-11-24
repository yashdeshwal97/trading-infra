package models

import constants.enums.*
import java.util.UUID

class Order {
    var id: String
    val symbol: String
    val exchange: ExchangeName
    val exchangeId: String
    val price: Double
    val amount: Double
    val side: Side
    val type: OrderType
    val postOnly: Boolean
    val reduceOnly: Boolean
    var ioc: Boolean
    var exchangeOrderId: String? = null
    var status: OrderStatus = OrderStatus.OPEN
    var filledAmount: Double = 0.0
    var filledPrice: Double = 0.0
    var fee: Double = 0.0
    var feeCurrency: String = ""
    var timeStamp: Long = 0
    var metadata: Map<String, Any>? = null
    var rejectionCode: Int = -1
    var rejectionReason: String = ""
    var isCancelled: Boolean = false
    var cancelSent: Boolean = false

    constructor(
        symbol: String,
        exchange: ExchangeName,
        exchangeId: String,
        price: Double,
        amount: Double,
        side: Side,
        type: OrderType,
        postOnly: Boolean,
        reduceOnly: Boolean,
        ioc: Boolean
    ) {
        id = UUID.randomUUID().toString()
        timeStamp = System.currentTimeMillis()
        this.symbol = symbol
        this.exchange = exchange
        this.exchangeId = exchangeId
        this.price = price
        this.amount = amount
        this.side = side
        this.type = type
        this.postOnly = postOnly
        this.reduceOnly = reduceOnly
        this.ioc = ioc
    }

    fun duplicate(): Order {
        var newOrder = Order(
            this.symbol,
            this.exchange,
            this.exchangeId,
            this.price,
            this.amount,
            this.side,
            this.type,
            this.postOnly,
            this.reduceOnly,
            this.ioc
        )
        newOrder.id = this.id
        newOrder.exchangeOrderId = this.exchangeOrderId
        newOrder.fee = this.fee
        newOrder.feeCurrency = this.feeCurrency
        newOrder.metadata = this.metadata
        newOrder.isCancelled = this.isCancelled
        newOrder.timeStamp = this.timeStamp
        newOrder.rejectionCode = this.rejectionCode
        newOrder.rejectionReason = this.rejectionReason
        newOrder.status = this.status
        newOrder.filledAmount = this.filledAmount
        newOrder.filledPrice = this.filledPrice
        return newOrder
    }

    override fun toString(): String {
        var str = StringBuilder("")
        str.append(
            "ID=$id | " +
                    "Symbol=${symbol} | " +
                    "Exchange=${exchange.name} | " +
                    "ExchangeId=${exchangeId} | " +
                    "Status=${status} | " +
                    "Price=${price} | " +
                    "Amount=${amount} | " +
                    "Side=${side} | " +
                    "Type=${type} | " +
                    "postOnly=${postOnly}  | " +
                    "reduceOnly=${reduceOnly}  | " +
                    "ioc=${ioc}  | " +
                    "ExchangeOrderId=${exchangeOrderId} | " +
                    "FilledPrice=${filledPrice} | " +
                    "FilledAmount=${filledAmount} | " +
                    "Fee=${fee} | " +
                    "FeeCurrency=${feeCurrency} | " +
                    "TimeStamp=${timeStamp} | " +
                    "Cancelled=${isCancelled} | " +
                    "RejectionCode=${rejectionCode} | " +
                    "RejectionReason=${rejectionReason} | " +
                    "\n"
        )
        return str.toString()
    }

    open val openAmount : Double
        get() {
            return amount - filledAmount
        }


    open val isOpen: Boolean
        get() {
            return !isClosed
        }

    open val isClosed: Boolean
        get() {
            return this.status.isClosed
        }



}