package exchanges

class ExchangeSpecification(exchangeClass: Exchange) {
    var exchangeName: String? = null
    var exchangeDescription: String? = null
    var secretKey: String? = null
    var apiKey: String? = null
    var sslUri: String? = null
    var host: String? = null
    var port = 80
    var exchangeSpecificParameters = HashMap<String, Any>()

    fun setExchangeSpecificParametersItem(key: String, value: Any) {
        exchangeSpecificParameters.put(key, value)
    }
}