package exchanges.ftx

import models.ftx.account.FTXPosition
import exchanges.services.IAccountService
import exchanges.Exchange
import models.ftx.account.FTXAccount
import exchanges.base.executeCall
import models.ftx.FTXResult

class FTXAccountService(exchange: Exchange) : FTXBaseService(exchange), IAccountService {
    val account: FTXResult<FTXAccount>
        get() {
            return apiAuthenticated!!.getAccount(apiKey,
                exchange.nonceFactory.createValue().toString(),
                subaccount).executeCall()
        }

    val balances: FTXResult<List<Map<String, Any>>>
        get() {
            return apiAuthenticated!!.getBalances(apiKey,
                exchange.nonceFactory.createValue().toString(),
                subaccount).executeCall()
        }

    val allBalances: FTXResult<Map<String, Any>>
        get() {
            return apiAuthenticated!!.getAllBalances(apiKey,
                exchange.nonceFactory.createValue().toString(),
                subaccount).executeCall()
        }

    fun getDeposits(startTime: Long?, endTime: Long?): FTXResult<List<Map<String, Any>>> {
        return apiAuthenticated!!.getDeposits(apiKey,
            exchange.nonceFactory.createValue().toString(),
            subaccount, startTime, endTime).executeCall()
    }

    fun getWithdrawals(startTime: Long?, endTime: Long?): FTXResult<List<Map<String, Any>>> {
        return apiAuthenticated!!.getWithdrawals(apiKey,
            exchange.nonceFactory.createValue().toString(),
            subaccount, startTime, endTime).executeCall()
    }

    fun getPositions(): FTXResult<List<FTXPosition>> {
        return apiAuthenticated!!.getPositions(apiKey,
            exchange.nonceFactory.createValue().toString(),
            subaccount).executeCall()
    }
}