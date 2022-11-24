package services

import constants.enums.ExchangeName
import models.Contract
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import java.io.FileReader

object ContractManager {

    private val logger = KotlinLogging.logger {}

    var symbols = mutableListOf<String>()
    var currencies = HashMap<String, Contract>()
    val currencyMap = HashMap<ExchangeName, HashMap<String, Contract>>()

    fun load(configFile: String) {
        val yaml = Yaml()
        val config: HashMap<String, Any> = HashMap()
        config.putAll(yaml.load(FileReader(configFile)) as Map<String, Any>)
        load(config)
    }

    fun load(config: Map<String, Any>) {
        config.forEach { (exchName, sconfigs) ->
            (sconfigs as Map<String, Any>).forEach { (id, config) ->
                val fullConfig = HashMap<String, Any>(config as Map<String, Any>)
                fullConfig["exchange"] = exchName
                val sContract = Contract(config, exchName)
                currencies[id] = sContract
                if(sContract.exchange !in currencyMap) {
                    currencyMap!![sContract.exchange] = HashMap()
                }
                currencyMap[sContract.exchange]!![sContract.symbol] = sContract
            }
        }
    }

    fun get(id: String) : Contract {
        return currencies[id]!!
    }

    fun getContract(exchangeName: ExchangeName, symbol: String) : Contract {
        return currencyMap[exchangeName]!![symbol]!!
    }
}