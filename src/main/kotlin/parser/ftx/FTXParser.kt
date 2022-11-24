package parser.ftx

import constants.enums.ExchangeName
import constants.enums.Side
import models.Snapshot

object FTXParser {
    fun onSnapshot(message: Map<*, *>): Snapshot {
        val data = message["data"] as Map<*, *>
        val snapshot = Snapshot(ExchangeName.FTX)
        listOf("bids", "asks").forEach {side ->
            val s = if(side == "bids") Side.BID else Side.ASK
            val sideData = data[side] as List<List<Double>>
            sideData.forEach { d ->
                val price = d[0]
                val amount = d[1]
                snapshot.addOrEdit(s, price, amount)
            }
        }
        return snapshot
    }
}