package trader

import mu.KotlinLogging

object TraderPositionManager {
    private val logger = KotlinLogging.logger {}

    val positions = HashMap<String, HashMap<String, TraderPosition>>()

    fun getPosition(exchangeId: String, contractName: String): TraderPosition {
        if(exchangeId !in positions) {
            positions[exchangeId] = HashMap()
        }

        if(contractName !in positions[exchangeId]!!) {
            positions[exchangeId]!![contractName] = TraderPosition(exchangeId, contractName)
        }

        return positions[exchangeId]!![contractName]!!
    }

}