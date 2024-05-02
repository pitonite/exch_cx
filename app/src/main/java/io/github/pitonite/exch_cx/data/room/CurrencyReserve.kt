package io.github.pitonite.exch_cx.data.room

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import io.github.pitonite.exch_cx.utils.DateSerializer
import java.math.BigDecimal
import java.util.Date
import kotlinx.serialization.Serializable

@Entity
@Stable
@Serializable
data class CurrencyReserve(
    @PrimaryKey val currency: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    @Serializable(with = DateSerializer::class) val updatedAt: Date = Date(),
)
