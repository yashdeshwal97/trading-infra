package exchanges

import exchanges.services.IAccountService
import exchanges.services.IMarketDataService
import exchanges.services.ITradeService
import exchanges.utils.SynchronizedValueFactory

interface Exchange {
    var tradeService: ITradeService
    var marketDataService: IMarketDataService
    var accountService: IAccountService
    val nonceFactory: SynchronizedValueFactory<Long>
    fun getDefaultExchangeSpecification(): ExchangeSpecification
}