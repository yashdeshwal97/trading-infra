package configs

import org.yaml.snakeyaml.Yaml
import java.io.FileReader

object ProxyConfig {

    val properties = HashMap<String, Map<String, Any>>()

    fun initialize(configFile: String) {
        val yaml = Yaml()
        var proxyConfigs: HashMap<String, Map<String, Any>> = HashMap()
        proxyConfigs.putAll(yaml.load(FileReader(configFile)) as Map<String, Map<String, Any>>)
        properties.putAll(proxyConfigs)

    }

    fun getProperty(proxyId: String, name: String): Any? {
        if(proxyId in properties) {
            val properties = properties[proxyId] as Map<String, Any>
            if(name in properties){
                return properties[name]
            }
        }

        return null
    }

    fun hasProperty(proxyId: String, name: String): Boolean {
        if(proxyId in properties) {
            val properties = properties[proxyId] as Map<String, Any>
            if(name in properties){
                return true
            }
        }

        return false
    }

    fun getProxies(proxyId: String): Map<String, Any> {
        return properties[proxyId]!!
    }


    val proxyIds: Set<String>
        get() = properties.keys

}