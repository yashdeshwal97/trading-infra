package services

import models.Position

class PositionManager {
    var symbol: String
    var exchangeId: String
    var positions: HashMap<String, Position> = HashMap()
    constructor(exchangeId: String, symbol: String) {
        this.exchangeId = exchangeId
        this.symbol = symbol
    }
}