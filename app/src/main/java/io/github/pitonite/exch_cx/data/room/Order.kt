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
import io.github.pitonite.exch_cx.model.api.OrderWalletPool
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum
import kotlinx.serialization.SerialName
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
    /** Minimum amount of from_currency to deposit */
    val maxInput: BigDecimal,
    /** Maximum amount of from_currency to deposit */
    val minInput: BigDecimal,
    @ColumnInfo(defaultValue = "null") val networkFee: BigDecimal? = null,
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    val state: CodifiedEnum<OrderState, String>,
    @ColumnInfo(defaultValue = "null")
    val stateError: CodifiedEnum<OrderStateError, String>? = null,
    val svcFee: BigDecimal,
    /** Amount of to_currency to be sent (null when no amount received yet) */
    @ColumnInfo(defaultValue = "null") val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    /** Transaction ID for from_currency received (null when no amount received yet) */
    @ColumnInfo(defaultValue = "null") val transactionIdReceived: String? = null,
    /** Transaction ID for to_currency sent (null when exchange not finished yet) */
    @ColumnInfo(defaultValue = "null") val transactionIdSent: String? = null,
    // newly added
    @ColumnInfo(defaultValue = "0") val refundAvailable: Boolean = false,
    /** Private key in case an ETH token is refunded in the REFUNDED state (when from_currency is one of USDC, DAI, USDT) */
    @ColumnInfo(defaultValue = "null") val refundPrivateKey: String? = null,
    @ColumnInfo(defaultValue = "null") val walletPool: CodifiedEnum<OrderWalletPool, String>? = null,
    //
    // custom added data:
    //
    @ColumnInfo(defaultValue = "null") val fromAmount: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val referrerId: String? = null,
    @ColumnInfo(defaultValue = "null") val aggregationOption: AggregationOption? = null,
    @ColumnInfo(defaultValue = "null") val feeOption: NetworkFeeOption? = null,
    @ColumnInfo(defaultValue = "null") val refundAddress: String? = null,
    // fetched using an api later by user:
    @ColumnInfo(defaultValue = "null") val letterOfGuarantee: String? = null,
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
  val walletPool: CodifiedEnum<OrderWalletPool, String>? = null,
  val refundAvailable: Boolean = false,
  val refundPrivateKey: String? = null,
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
    val walletPool: CodifiedEnum<OrderWalletPool, String>? = null,
    val refundAvailable: Boolean = false,
    val refundPrivateKey: String? = null,
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

@Stable
data class OrderLetterOfGuarantee(
  val id: String,
  val letterOfGuarantee: String,
)


@Stable
data class OrderToAddress(
  val id: String,
  val toAddress: String,
)
@Stable
data class OrderRefundAddress(
  val id: String,
  val refundAddress: String,
)

