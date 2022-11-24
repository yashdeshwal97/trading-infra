package exchanges.ftx

import exchanges.services.IMarketDataService
import exchanges.Exchange

class FTXMarketDataService(exchange: Exchange) : FTXBaseService(exchange), IMarketDataService