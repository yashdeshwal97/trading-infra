package services

import configs.TraderConfiguration
import mu.KotlinLogging
import trader.Market

class Test {
    companion object {

        val logger = KotlinLogging.logger {}
        @JvmStatic
        fun main(args: Array<String>) {
            TraderConfiguration.initialize("./configFile/config.yml")
            val markets = mutableListOf<Market>()
            val mBtc = Market("ftx", ContractManager.get("bnb_usd_spot_ftx"), 5, 5)
            mBtc.setup()
//            val mEth = Market("ftx", ContractManager.get("eth_usdt_spot_ftx"), 5, 5)
//            mEth.setup()
            val mEthUsd = Market("ftx", ContractManager.get("sol_usd_spot_ftx"), 5, 5)
            mEthUsd.setup()
            val mUsdt = Market("ftx", ContractManager.get("trx_usd_spot_ftx"), 5, 5)
            mUsdt.setup()
            markets.add(mBtc)
//            markets.add(mEth)
            markets.add(mEthUsd)
            markets.add(mUsdt)
            ExchangeOverLord.start()
            Thread.sleep(3000)
//            val pricer = Pricer(300, markets, "/Users/yashdeshwal/workspace/tayze/trading-engine/configFile/config.yml")
            Thread.sleep(3000)
//            val quote = pricer.getQuote("USDT", "ETH", 7.0)
//            val quote2 = pricer.getQuote(listOf("USDT"), "ETH", quote, listOf(50.86))
//            logger.info { "quotation : ${quote}" }
//            pricer.swap("USDT", "ETH", 7.0)
//            pricer.swap(listOf("USDT", "ETH"), "ETH", 0.006, listOf(33.57))
            /*Thread.sleep(2000)
            val exchanges = ExchangeConfig.authExchanges
            exchanges.forEach {
                var orders = mutableListOf<Order>()
                var count = 2
                while(count != 0) {
                    val newOrder =
                        Order("USDT/USD", ExchangeName.FTX, it, 1.0, 0.5, Side.ASK, OrderType.LIMIT, false, false, false)
                        orders.add(newOrder)
                    count = count - 1
                }
                val newOrder =
                    Order("USDT/USD", ExchangeName.FTX, it, 1.0, 0.5, Side.ASK, OrderType.LIMIT, false, false, false)
                orders.add(newOrder)
                OrderManager.sendNewOrder(orders)
                Thread.sleep(7000)
//                OrderManager.getPositions(newOrder.exchangeId)
                OrderManager.getBalances(newOrder.exchangeId)
                OrderManager.cancelAll("USDT/USD", newOrder.exchangeId)
  //              Thread.sleep(10000)
  //              OrderManager.cancelOrder(orders)
            }*/
        }
    }
}