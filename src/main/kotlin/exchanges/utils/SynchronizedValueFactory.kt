package exchanges.utils

interface SynchronizedValueFactory<T> {
    fun createValue(): T
}