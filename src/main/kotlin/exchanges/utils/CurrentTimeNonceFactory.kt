package exchanges.utils

class CurrentTimeNonceFactory: SynchronizedValueFactory<Long> {
    override fun createValue(): Long {
        return System.currentTimeMillis()
    }
}