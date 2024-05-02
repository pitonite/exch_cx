package io.github.pitonite.exch_cx.data.room

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pitonite.exch_cx.utils.DateSerializer
import io.github.pitonite.exch_cx.utils.NullableBigDecimalSerializer
import java.math.BigDecimal
import java.util.Date
import kotlinx.serialization.Serializable

@Entity(
    indices = [Index("createdAt"), Index("isEnabled")],
)
@Stable
@Serializable
data class CurrencyReserveTrigger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currency: String,
    @Serializable(with = NullableBigDecimalSerializer::class) val targetAmount: BigDecimal? = null,
    // null means always trigger this regardless of reserve amount.
    // -1 meaning new reserve amount is less than targetAmount,
    //  0 means equals,
    //  1 means new reserve amount more than targetAmount
    val comparison: Int? = 1, // -1, 0, 1, null
    @ColumnInfo(defaultValue = "1") val isEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "0") val onlyOnce: Boolean = false,
    @Serializable(with = DateSerializer::class) val createdAt: Date = Date(),
)
