package io.github.pitonite.exch_cx.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import java.math.BigDecimal
import java.util.Date
import javax.annotation.concurrent.Immutable

@Entity(
    indices = [Index("createdAt")],
)
@Immutable
data class Order(
    // data for ui:
    @ColumnInfo(defaultValue = CURRENT_TIMESTAMP_EXPRESSION) val modifiedAt: Date = Date(),
    @ColumnInfo(defaultValue = "FALSE")
    val archived: Boolean = false, // for moving orders to history
    // order data (api/v1):
    @PrimaryKey val id: String,
    val createdAt: Date = Date(),
    val fromAddr: String = "_GENERATING_",
    val fromCurrency: String,
    val fromAmountReceived: BigDecimal? = null,
    val maxInput: BigDecimal? = null,
    val minInput: BigDecimal? = null,
    val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: OrderState,
    val stateError: String? = null, // todo, replace with error enum
    val svcFee: BigDecimal,
    val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    val transactionIdReceived: String? = null,
    val transactionIdSent: String? = null,
    // custom added data:
    val calculatedFromAmount: BigDecimal? = null,
    val calculatedToAmount: BigDecimal? = null,
)

// to not touch archive when updating order
@Immutable
data class OrderUpdate(
    val id: String,
    val createdAt: Date = Date(),
    val modifiedAt: Date = Date(),
    val fromAddr: String = "_GENERATING_",
    val fromCurrency: String,
    val fromAmountReceived: BigDecimal? = null,
    val maxInput: BigDecimal? = null,
    val minInput: BigDecimal? = null,
    val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: OrderState,
    val stateError: String? = null, // todo, replace with error enum
    val svcFee: BigDecimal,
    val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    val transactionIdReceived: String? = null,
    val transactionIdSent: String? = null,
    val calculatedFromAmount: BigDecimal? = null,
    val calculatedToAmount: BigDecimal? = null,
)
