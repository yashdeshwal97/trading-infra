package configs

import constants.enums.ExchangeName
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import services.ContractManager
import java.io.FileReader
import java.util.*
import kotlin.collections.HashMap

object ExchangeConfig {

    val mdExchangeProperties = HashMap<ExchangeName, Map<String, Any>>()
    val authenticatedExchangeProperties = HashMap<String, Map<String, Any>>()
    var mdConnectionCount = 1

    private val logger = KotlinLogging.logger {}

    fun initialize(exConfigs: Map<String, Any>) {

        mdConnectionCount = if("mdConnectionCount" in exConfigs) {
            (exConfigs["mdConnectionCount"] as Number).toInt()
        } else {
            1
        }

        val mdConfigs = exConfigs["md"] as Map<String, Any>

        for ((exStr, exConfigT) in mdConfigs) {
            val exConfig = exConfigT as Map<String, Any>
            val exchange = ExchangeName.valueOf(exStr.uppercase(Locale.getDefault()))
            mdExchangeProperties[exchange] = exConfig
            val currencyFile = exConfig["currenciesFile"] as String
            ContractManager.load(currencyFile)
            logger.info{"${exchange} MD Properties: ${exConfig}"}
        }

        if("auth" in exConfigs) {
            val authConfigs = exConfigs["auth"] as Map<String, Any>

            for ((exStr, exConfigT) in authConfigs) {
                val exConfig = exConfigT as Map<String, Any>
                val exchange = ExchangeName.valueOf(exStr.uppercase(Locale.getDefault()))
                authenticatedExchangeProperties[exStr] = exConfig
                logger.info { "${exchange} Auth Properties: ${exConfig}" }
            }
        }
    }

    fun initialize(configFile: String) {
        val yaml = Yaml()
        var exConfigs: HashMap<String, Any> = HashMap()
        exConfigs.putAll(yaml.load(FileReader(configFile)) as Map<String, Any>)

        for ((exStr, exConfigT) in exConfigs) {
            val exConfig = exConfigT as Map<String, Any>
            val exchange = ExchangeName.valueOf(exStr.uppercase(Locale.getDefault()))
            mdExchangeProperties[exchange] = exConfig
            val currencyFile = exConfig["currenciesFile"] as String
            ContractManager.load(currencyFile)
            logger.info{"${exchange} Properties: ${exConfig}"}
        }
    }

    fun getMDProperty(exchange: ExchangeName, name: String): Any? {
        if(exchange in mdExchangeProperties) {
            val properties = mdExchangeProperties[exchange] as Map<String, Any>
            if(name in properties){
                return properties[name]
            }
        }

        return null
    }

    fun getAuthProperty(id: String, name: String): Any? {
        if(id in authenticatedExchangeProperties) {
            val properties = authenticatedExchangeProperties[id] as Map<String, Any>
            if(name in properties){
                return properties[name]
            }
        }

        return null
    }

    fun hasMDProperty(exchange: ExchangeName, name: String): Boolean {
        if(exchange in mdExchangeProperties) {
            val properties = mdExchangeProperties[exchange] as Map<String, Any>
            if(name in properties){
                return true
            }
        }

        return false
    }

    fun hasAuthProperty(id: String, name: String): Boolean {
        if(id in authenticatedExchangeProperties) {
            val properties = authenticatedExchangeProperties[id] as Map<String, Any>
            if(name in properties){
                return true
            }
        }

        return false
    }

    fun getMDProperties(exchange: ExchangeName): Map<String, Any> {
        return mdExchangeProperties[exchange]!!
    }

    fun getAuthProperties(id: String): Map<String, Any> {
        return authenticatedExchangeProperties[id]!!
    }


    val mdExchanges: Set<ExchangeName>
        get() = mdExchangeProperties.keys

    val authExchanges: Set<String>
        get() = authenticatedExchangeProperties.keys

}