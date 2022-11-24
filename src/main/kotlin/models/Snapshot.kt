package models

import constants.enums.ExchangeName
import constants.enums.Side

data class Snapshot(val exchangeName: ExchangeName) {
    val obSide = mapOf(
        Side.BID to SnapshotSide(Side.BID),
        Side.ASK to SnapshotSide(Side.ASK)
    )
    fun addOrEdit(side: Side, price: Double, amount: Double) : Unit {
        obSide[side]!!.addLevel(price, amount)
    }
}