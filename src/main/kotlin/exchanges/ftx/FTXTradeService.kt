package exchanges.ftx

import exchanges.services.ITradeService
import exchanges.Exchange
import models.ftx.trade.FTXOrder
import models.ftx.requests.FTXCancelRequest
import models.ftx.requests.FTXOrderRequest
import exchanges.base.executeCall
import models.ftx.FTXResult

class FTXTradeService(exchange: Exchange) : FTXBaseService(exchange), ITradeService {
    fun getOrderByClientId(id: String): FTXResult<FTXOrder> {
        return apiAuthenticated!!.getOrderByClientId(apiKey,
            exchange.nonceFactory.createValue().toString(), subaccount,
            id).executeCall()

    }

    fun placeNewOrder(request: FTXOrderRequest): FTXResult<FTXOrder> {
        return apiAuthenticated!!.placeNewOrder(apiKey,
            exchange.nonceFactory.createValue().toString(), subaccount,
            request).executeCall()
    }

    fun cancelByClientId(clientId: String): FTXResult<String> {
        return  apiAuthenticated!!.cancelByClientId(apiKey,
            exchange.nonceFactory.createValue().toString(),
            subaccount, clientId).executeCall()
    }

    fun cancelAll(market: String?): FTXResult<String> {
        return apiAuthenticated!!.cancelAll(apiKey,
            exchange.nonceFactory.createValue().toString(),
            subaccount, FTXCancelRequest(market)
        ).executeCall()
    }

    fun fills(market: String?, startTime: Long?, endTime: Long?) : FTXResult<List<Map<String, Any>>> {
        return apiAuthenticated!!.fills(apiKey,
            exchange.nonceFactory.createValue().toString(),
            subaccount, market, startTime, endTime).executeCall()
    }
}