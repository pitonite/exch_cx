package io.github.pitonite.exch_cx.data.room

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.pitonite.exch_cx.model.api.AggregationOption
import io.github.pitonite.exch_cx.model.api.NetworkFeeOption
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.OrderStateError
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum
import java.math.BigDecimal
import java.util.Date

const val GENERATING_FROM_ADDRESS = "_GENERATING_"

@Entity(
    indices = [Index("createdAt"), Index("archived", "createdAt")],
)
@Stable
data class Order(
    // data for ui:
    @ColumnInfo(defaultValue = CURRENT_TIMESTAMP_EXPRESSION) val modifiedAt: Date = Date(),
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false, // for moving orders to history
    // order data (api/v1):
    @PrimaryKey val id: String,
    val createdAt: Date = Date(),
    @ColumnInfo(defaultValue = "'$GENERATING_FROM_ADDRESS'")
    val fromAddr: String = GENERATING_FROM_ADDRESS,
    val fromCurrency: String,
    @ColumnInfo(defaultValue = "null") val fromAmountReceived: BigDecimal? = null,
    val maxInput: BigDecimal,
    val minInput: BigDecimal,
    @ColumnInfo(defaultValue = "null") val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: CodifiedEnum<OrderState, String>,
    @ColumnInfo(defaultValue = "null")
    val stateError: CodifiedEnum<OrderStateError, String>? = null,
    val svcFee: BigDecimal,
    @ColumnInfo(defaultValue = "null") val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    @ColumnInfo(defaultValue = "null") val transactionIdReceived: String? = null,
    @ColumnInfo(defaultValue = "null") val transactionIdSent: String? = null,
    //
    // custom added data:
    //
    @ColumnInfo(defaultValue = "null") val fromAmount: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val referrerId: String? = null,
    @ColumnInfo(defaultValue = "null") val aggregationOption: AggregationOption? = null,
    @ColumnInfo(defaultValue = "null") val feeOption: NetworkFeeOption? = null,
    @ColumnInfo(defaultValue = "null") val refundAddress: String? = null,
)

// to not touch archive when updating order
@Stable
data class OrderUpdate(
    val id: String,
    val createdAt: Date = Date(),
    val modifiedAt: Date = Date(),
    val fromAddr: String = "_GENERATING_",
    val fromCurrency: String,
    val fromAmountReceived: BigDecimal? = null,
    val maxInput: BigDecimal,
    val minInput: BigDecimal,
    val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: CodifiedEnum<OrderState, String>,
    val stateError: CodifiedEnum<OrderStateError, String>? = null,
    val svcFee: BigDecimal,
    val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    val transactionIdReceived: String? = null,
    val transactionIdSent: String? = null,
)

@Stable
data class OrderUpdateWithArchive(
    val id: String,
    val archived: Boolean,
    val createdAt: Date = Date(),
    val modifiedAt: Date = Date(),
    val fromAddr: String = "_GENERATING_",
    val fromCurrency: String,
    val fromAmountReceived: BigDecimal? = null,
    val maxInput: BigDecimal,
    val minInput: BigDecimal,
    val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: CodifiedEnum<OrderState, String>,
    val stateError: CodifiedEnum<OrderStateError, String>? = null,
    val svcFee: BigDecimal,
    val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    val transactionIdReceived: String? = null,
    val transactionIdSent: String? = null,
)

/** for use when an order is initially created by sending a request to api */
@Stable
data class OrderCreate(
    val id: String,
    val createdAt: Date = Date(),
    val modifiedAt: Date = Date(),
    val fromCurrency: String,
    val toCurrency: String,
    val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: CodifiedEnum<OrderState, String> = OrderState.CREATED.codifiedEnum(),
    val svcFee: BigDecimal,
    val toAddress: String,
    val refundAddress: String? = null,
    val fromAmount: BigDecimal? = null,
    val maxInput: BigDecimal,
    val minInput: BigDecimal,
)

@Stable
data class OrderArchive(
    val id: String,
    val archived: Boolean,
)
