package models

import org.jetbrains.annotations.NotNull

class SwapRequestCurrency {
    @NotNull
    var inputCurrency: String? = null

    @NotNull
    var inputAmount: Double = 0.0
}