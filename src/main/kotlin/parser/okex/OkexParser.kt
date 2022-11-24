package parser.okex

import constants.enums.ExchangeName
import constants.enums.Side
import models.Snapshot

object OkexParser {
    fun onSnapshot(instrument: String, action: String, data: List<Map<*, *>>): Snapshot {
        val snapshot = Snapshot(ExchangeName.OKEXV5)
        data.forEach { d ->
            listOf("asks", "bids").forEach { sideStr ->
                val side = if (sideStr == "asks") Side.ASK else Side.BID
                val priceArrs = d[sideStr] as List<List<*>>

                priceArrs.forEach { priceArr ->
                    val price = (priceArr[0] as String).toDouble()
                    val amount = (priceArr[1] as String).toDouble()
                    snapshot.addOrEdit(side, price, amount)
                }
            }
        }
        return snapshot
    }
}