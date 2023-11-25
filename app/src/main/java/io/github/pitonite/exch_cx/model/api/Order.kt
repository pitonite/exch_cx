package io.github.pitonite.exch_cx.model.api

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.Translatable
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import io.github.pitonite.exch_cx.utils.codified.Codified
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.serializer.codifiedEnumSerializer
import java.math.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
enum class OrderState(override val code: String, override val translation: Int? = null) :
    Codified<String>, Translatable {
  CREATED("CREATED", R.string.order_state_created),
  CANCELLED("CANCELLED", R.string.order_state_cancelled),
  AWAITING_INPUT("AWAITING_INPUT", R.string.order_state_awaiting_input),
  CONFIRMING_INPUT("CONFIRMING_INPUT", R.string.order_state_confirming_input),
  EXCHANGING("EXCHANGING", R.string.order_state_exchanging),
  CONFIRMING_SEND("CONFIRMING_SEND", R.string.order_state_confirming_send),
  COMPLETE("COMPLETE", R.string.order_state_complete),
  REFUND_REQUEST("REFUND_REQUEST", R.string.order_state_refund_request),
  REFUND_PENDING("REFUND_PENDING", R.string.order_state_refund_pending),
  CONFIRMING_REFUND("CONFIRMING_REFUND", R.string.order_state_confirming_refund),
  REFUNDED("REFUNDED", R.string.order_state_refunded),
  BRIDGING("BRIDGING", R.string.order_state_bridging), // experimental
  FUNDED("FUNDED", R.string.order_state_funded); // experimental

  object CodifiedSerializer :
      KSerializer<CodifiedEnum<OrderState, String>> by codifiedEnumSerializer()
}

@Stable
enum class OrderStateError(override val code: String, override val translation: Int? = null) :
    Codified<String>, Translatable {
  NO_BALANCE_AVAILABLE_TO_SEND(
      "NO_BALANCE_AVAILABLE_TO_SEND", R.string.order_error_no_balance_available_to_send),
  INSTANCE_NOT_AVAILABLE("INSTANCE_NOT_AVAILABLE", R.string.order_error_instance_not_available),
  ERROR_GENERATING_ADDRESS(
      "ERROR_GENERATING_ADDRESS", R.string.order_error_error_generating_address),
  TO_ADDRESS_INVALID("TO_ADDRESS_INVALID", R.string.order_error_to_address_invalid);

  object CodifiedSerializer :
      KSerializer<CodifiedEnum<OrderStateError, String>> by codifiedEnumSerializer()
}

@Stable
enum class OrderWalletPool(override val code: String, override val translation: Int? = null) :
    Codified<String>, Translatable {
  AGGREGATED("AGGREGATED", R.string.aggregated),
  MIXED("MIXED", R.string.mixed),
  ANY("ANY", R.string.any),
  ;

  object CodifiedSerializer :
      KSerializer<CodifiedEnum<OrderWalletPool, String>> by codifiedEnumSerializer()
}

@Serializable
@Stable
data class OrderResponse(
    /** Exchange order ID */
    @SerialName("orderid") val id: String,
    /** Order creation timestamp (unix timestamp, in seconds) */
    @SerialName("created") val created: Long? = null,

    /**
     * Address for from_currency. this is the address user has to send crypto to in order to
     * initiate exchange. it can be set to "_GENERATING_" if address is not ready yet. for example,
     * "_GENERATING_" can happen if there's an error (state_error is set).
     */
    @SerialName("from_addr") val fromAddr: String,
    /** The currency the user has to send to "from_addr" */
    @SerialName("from_currency") val fromCurrency: String,
    /** Amount of "from_currency" received (null when no amount received yet) */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("from_amount_received")
    val fromAmountReceived: BigDecimal? = null,

    /** Maximum amount of from_currency to deposit */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("max_input")
    val maxInput: BigDecimal,
    /** Minimum amount of from_currency to deposit */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("min_input")
    val minInput: BigDecimal,

    /** Network fee (included in the calculated amount of to_currency) */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("network_fee")
    val networkFee: BigDecimal? = null,

    /** Current rate */
    @Serializable(with = BigDecimalSerializer::class) @SerialName("rate") val rate: BigDecimal,
    /** Rate mode ("flat" or "dynamic") */
    @SerialName("rate_mode") val rateMode: RateFeeMode,
    /**
     * Current state of the exchange, possible values: CREATED, CANCELLED, AWAITING_INPUT,
     * CONFIRMING_INPUT, EXCHANGING, CONFIRMING_SEND, COMPLETE, REFUND_REQUEST, REFUND_PENDING,
     * CONFIRMING_REFUND, REFUNDED
     */
    @Serializable(with = OrderState.CodifiedSerializer::class)
    @SerialName("state")
    val state: CodifiedEnum<OrderState, String>,
    /** Current error state, if any (only present on error). such as "TO_ADDRESS_INVALID" */
    @Serializable(with = OrderStateError.CodifiedSerializer::class)
    @SerialName("state_error")
    val stateError: CodifiedEnum<OrderStateError, String>? = null,
    /**
     * Service fee as a string representing a decimal number. (included in the calculated amount of
     * to_currency)
     */
    @Serializable(with = BigDecimalSerializer::class) @SerialName("svc_fee") val svcFee: BigDecimal,
    /** Amount of to_currency to be sent (null when no amount received yet) */
    @SerialName("to_amount")
    @Serializable(with = BigDecimalSerializer::class)
    val toAmount: BigDecimal? = null,
    /** The address that the exchanged crypto will be sent to */
    @SerialName("to_address") val toAddress: String,
    /**
     * The currency that user will receive after exchange is fulfilled. for example "XMR", "BTC",
     * etc.
     */
    @SerialName("to_currency") val toCurrency: String,

    /** Transaction ID for from_currency received (null when no amount received yet) */
    @SerialName("transaction_id_received") val transactionIdReceived: String? = null,
    /** Transaction ID for to_currency sent (null when exchange not finished yet) */
    @SerialName("transaction_id_sent") val transactionIdSent: String? = null,
    @Serializable(with = OrderWalletPool.CodifiedSerializer::class)
    @SerialName("wallet_pool")
    val walletPool: CodifiedEnum<OrderWalletPool, String>? = null,
    @SerialName("refund_available") val refundAvailable: Boolean = false,
    @SerialName("refund_private_key") val refundPrivateKey: String? = null,
    // 
    @SerialName("refund_transaction_id") val refundTransactionId: String? = null,
    @SerialName("refund_addr") val refundAddress: String? = null,
    @SerialName("refund_fee_amount")
    @Serializable(with = BigDecimalSerializer::class)
    val refundFeeAmount: BigDecimal? = null,
)
