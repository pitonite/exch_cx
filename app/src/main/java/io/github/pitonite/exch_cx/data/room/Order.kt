package io.github.pitonite.exch_cx.data.room

import androidx.compose.runtime.Immutable
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
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import io.github.pitonite.exch_cx.utils.DateSerializer
import io.github.pitonite.exch_cx.utils.NullableBigDecimalSerializer
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Date

const val GENERATING_FROM_ADDRESS = "_GENERATING_"

@Serializable
@Entity(
    indices = [Index("createdAt"), Index("archived", "createdAt")],
)
@Stable
data class Order(
    // order data (api/v1):
    @PrimaryKey val id: String,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date = Date(),
    @ColumnInfo(defaultValue = "'$GENERATING_FROM_ADDRESS'")
    val fromAddr: String = GENERATING_FROM_ADDRESS,
    val fromCurrency: String,
    @Serializable(with = NullableBigDecimalSerializer::class)
    @ColumnInfo(defaultValue = "null") val fromAmountReceived: BigDecimal? = null,
    /** Minimum amount of from_currency to deposit */
    @Serializable(with = BigDecimalSerializer::class)
    val maxInput: BigDecimal,
    /** Maximum amount of from_currency to deposit */
    @Serializable(with = BigDecimalSerializer::class)
    val minInput: BigDecimal,
    @Serializable(with = NullableBigDecimalSerializer::class)
    @ColumnInfo(defaultValue = "null") val networkFee: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val rate: BigDecimal,
    val rateMode: RateFeeMode,
    @Serializable(with = OrderState.CodifiedSerializer::class)
    val state: CodifiedEnum<OrderState, String>,
    @ColumnInfo(defaultValue = "null")
    @Serializable(with = OrderStateError.CodifiedSerializer::class)
    val stateError: CodifiedEnum<OrderStateError, String>? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val svcFee: BigDecimal,
    /** Amount of to_currency to be sent (null when no amount received yet) */
    @Serializable(with = NullableBigDecimalSerializer::class)
    @ColumnInfo(defaultValue = "null") val toAmount: BigDecimal? = null,
    val toAddress: String,
    val toCurrency: String,
    /** Transaction ID for from_currency received (null when no amount received yet) */
    @ColumnInfo(defaultValue = "null") val transactionIdReceived: String? = null,
    /** Transaction ID for to_currency sent (null when exchange not finished yet) */
    @ColumnInfo(defaultValue = "null") val transactionIdSent: String? = null,
    // newly added
    @ColumnInfo(defaultValue = "0") val refundAvailable: Boolean = false,
    /**
     * Private key in case an ETH token is refunded in the REFUNDED state (when from_currency is one
     * of USDC, DAI, USDT)
     */
    @ColumnInfo(defaultValue = "null") val refundPrivateKey: String? = null,
    @ColumnInfo(defaultValue = "null")
    @Serializable(with = OrderWalletPool.CodifiedSerializer::class)
    val walletPool: CodifiedEnum<OrderWalletPool, String>? = null,
    @ColumnInfo(defaultValue = "null") val refundTransactionId: String? = null,
    @ColumnInfo(defaultValue = "null") val refundAddress: String? = null,
    @Serializable(with = NullableBigDecimalSerializer::class)
    @ColumnInfo(defaultValue = "null") val refundFeeAmount: BigDecimal? = null,
    //
    // custom added data:
    //
    @Serializable(with = DateSerializer::class)
    @ColumnInfo(defaultValue = CURRENT_TIMESTAMP_EXPRESSION) val modifiedAt: Date = Date(),
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false, // for moving orders to history
    //
    @Serializable(with = NullableBigDecimalSerializer::class)
    @ColumnInfo(defaultValue = "null") val fromAmount: BigDecimal? = null,
    @ColumnInfo(defaultValue = "null") val referrerId: String? = null,
    @ColumnInfo(defaultValue = "null") val aggregationOption: AggregationOption? = null,
    @ColumnInfo(defaultValue = "null") val feeOption: NetworkFeeOption? = null,
    // fetched using an api while order is being updated:
    @ColumnInfo(defaultValue = "null") val letterOfGuarantee: String? = null,
    @ColumnInfo(defaultValue = "0") val deletedInRemote: Boolean = false,
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
    val refundTransactionId: String? = null,
    val refundAddress: String? = null,
    val refundFeeAmount: BigDecimal? = null,
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
    val refundTransactionId: String? = null,
    val refundAddress: String? = null,
    val refundFeeAmount: BigDecimal? = null,
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

@Stable
data class OrderDeletedInRemote(
    val id: String,
    val deletedInRemote: Boolean,
)
