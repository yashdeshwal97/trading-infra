package constants.enums

enum class OrderStatus {
    OPEN,
    CLOSED,
    REJECTED;

    val isClosed: Boolean
        get() {
            return this == CLOSED || this == REJECTED
        }
}