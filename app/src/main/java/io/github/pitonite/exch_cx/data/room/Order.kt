package io.github.pitonite.exch_cx.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pitonite.exch_cx.model.api.AggregationOption
import io.github.pitonite.exch_cx.model.api.NetworkFeeOption
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import java.math.BigDecimal
import java.util.Date
import javax.annotation.concurrent.Immutable

@Entity(
    indices = [Index("createdAt"), Index("archived", "createdAt")],
)
@Immutable
data class Order(
    // data for ui:
    @ColumnInfo(defaultValue = CURRENT_TIMESTAMP_EXPRESSION) val modifiedAt: Date = Date(),
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false, // for moving orders to history
    // order data (api/v1):
    @PrimaryKey val id: String,
    val createdAt: Date = Date(),
    @ColumnInfo(defaultValue = "'_GENERATING_'") val fromAddr: String = "_GENERATING_",
    val fromCurrency: String,
    @ColumnInfo(defaultValue = "null") val fromAmountReceived: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val maxInput: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val minInput: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: OrderState,
    @ColumnInfo(defaultValue = "null")
    val stateError: String? = null, // todo, replace with error enum
    val svcFee: BigDecimal,
    @ColumnInfo(defaultValue = "null") val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    @ColumnInfo(defaultValue = "null") val transactionIdReceived: String? = null,
    @ColumnInfo(defaultValue = "null") val transactionIdSent: String? = null,
    //
    // custom added data:
    //
    @ColumnInfo(defaultValue = "null") val calculatedFromAmount: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val calculatedToAmount: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val referrerId: String? = null,
    @ColumnInfo(defaultValue = "null") val aggregationOption: AggregationOption? = null,
    @ColumnInfo(defaultValue = "null") val feeOption: NetworkFeeOption? = null,
    @ColumnInfo(defaultValue = "null") val refundAddress: String? = null,
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

@Immutable
data class OrderUpdateWithArchive(
    val id: String,
    val archived: Boolean,
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

/** for use when an order is initially created by sending a request to api */
@Immutable
data class OrderCreate(
    val id: String,
    val createdAt: Date = Date(),
    val modifiedAt: Date = Date(),
    val fromCurrency: String,
    val toCurrency: String,
    val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: OrderState = OrderState.CREATED,
    val svcFee: BigDecimal,
    val toAddress: String,
    val refundAddress: String? = null,
    val calculatedFromAmount: BigDecimal? = null,
    val calculatedToAmount: BigDecimal? = null,
)

@Immutable
data class OrderArchive(
    val id: String,
    val archived: Boolean,
)
