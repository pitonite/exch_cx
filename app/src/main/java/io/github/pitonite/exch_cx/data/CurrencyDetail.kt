package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Immutable
import java.math.BigDecimal

@Immutable
data class CurrencyDetail(
    val name: String,
    val reserve: BigDecimal,
)
