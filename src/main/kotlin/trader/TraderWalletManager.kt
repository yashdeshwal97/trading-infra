package trader

import mu.KotlinLogging

object TraderWalletManager {
    private val logger = KotlinLogging.logger {}

    val positions = HashMap<String, HashMap<String, TraderPosition>>()

    fun getBalance(exchangeId: String, coinName: String): TraderPosition {
        if(exchangeId !in positions) {
            positions[exchangeId] = HashMap()
        }

        if(coinName !in positions[exchangeId]!!) {
            positions[exchangeId]!![coinName] = TraderPosition(exchangeId, coinName)
        }

        return positions[exchangeId]!![coinName]!!
    }
}