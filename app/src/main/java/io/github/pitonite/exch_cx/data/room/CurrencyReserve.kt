package io.github.pitonite.exch_cx.data.room

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

@Entity
@Stable
data class CurrencyReserve(
    @PrimaryKey val currency: String,
    val amount: BigDecimal,
    val updatedAt: Date = Date(),
)
