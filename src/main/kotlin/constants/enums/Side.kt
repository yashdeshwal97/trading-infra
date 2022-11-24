package constants.enums

enum class Side {

        /** Buying order (the trader is providing the counter currency)  */
        BID,

        /** Selling order (the trader is providing the base currency)  */
        ASK;

        val opposite: Side
                get() {
                        return if(this == BID) {
                                ASK
                        } else {
                                BID
                        }
                }

        val m: Double
                get() {
                        return if(this == BID) {
                                1.0
                        } else {
                                -1.0
                        }
                }
}