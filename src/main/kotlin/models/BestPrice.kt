package models

import trader.Market

class BestPrice {
    var market: Market?
    var price: Double

    constructor(market: Market?, price: Double) {
        this.market = market
        this.price = price
    }
}