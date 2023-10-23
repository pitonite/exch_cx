package io.github.pitonite.exch_cx.model

import androidx.compose.runtime.Immutable
import java.math.BigDecimal

@Immutable
data class CurrencyDetail(
    val name: String,
    val reserve: BigDecimal,
)
