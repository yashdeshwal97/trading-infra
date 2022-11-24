package exchanges.ftx

import exchanges.services.IAccountService
import exchanges.services.IMarketDataService
import exchanges.services.ITradeService
import exchanges.utils.CurrentTimeNonceFactory
import exchanges.utils.SynchronizedValueFactory
import exchanges.Exchange
import exchanges.ExchangeSpecification

class FTXExchange: Exchange {

    val exchangeSpecification = ExchangeSpecification(this)
    override val nonceFactory: SynchronizedValueFactory<Long> = CurrentTimeNonceFactory()
    override var marketDataService: IMarketDataService
    override var tradeService: ITradeService
    override var accountService: IAccountService

    constructor(properties: Map<String, Any>) {
        exchangeSpecification.sslUri = "https://ftx.com"
        exchangeSpecification.host = "ftx.com"
        exchangeSpecification.port = 80
        exchangeSpecification.exchangeName = "FTX"
        exchangeSpecification.exchangeDescription = ""
        exchangeSpecification.apiKey = properties["apiKey"] as String
        exchangeSpecification.secretKey = properties["secret"] as String
        if ("subaccount" in properties) {
            exchangeSpecification.setExchangeSpecificParametersItem("subaccount", properties["subaccount"] as String)
        }
        marketDataService = FTXMarketDataService(this)
        tradeService = FTXTradeService(this)
        accountService = FTXAccountService(this)
    }

    override fun getDefaultExchangeSpecification(): ExchangeSpecification {
        return exchangeSpecification
    }
}