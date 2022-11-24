package configs

import constants.common.Constants
import org.yaml.snakeyaml.Yaml
import java.io.FileReader

object TraderConfiguration {

    private var config: HashMap<String, Any> = HashMap()

    fun initialize(configFile: String) {
        val yaml = Yaml()
        config.putAll(yaml.load(FileReader(configFile)) as Map<String, Any>)

        if("exchangeFile" in config) {
            val exFile = config["exchangesFile"] as String
            ExchangeConfig.initialize(exFile)
        }

        if("exchanges" in config) {
            val exConfigs = config["exchanges"] as Map<String, Any>
            ExchangeConfig.initialize(exConfigs)
        }

        if("proxyFile" in config) {
            val proxyFile = config["proxyFile"] as String
            ProxyConfig.initialize(proxyFile)
        }

        if("microLevels" in config) {
            Constants.microLevels = (config["microLevels"] as Number).toInt()
        }
    }

    fun hasProperty(name: String) = name in config

    fun getProperty(name: String): Any? {
        return config.get(name)
    }

}