package io.github.pitonite.exch_cx.model

import androidx.compose.runtime.Immutable
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

enum class OrderState {
  CREATED,
  CANCELLED,
  AWAITING_INPUT,
  CONFIRMING_INPUT,
  EXCHANGING,
  CONFIRMING_SEND,
  COMPLETE,
  REFUND_REQUEST,
  REFUND_PENDING,
  CONFIRMING_REFUND,
  REFUNDED
}

@Serializable
@Immutable
data class Order(
    /** Exchange order ID */
    @SerialName("orderid") val id: String? = null,
    /** Order creation timestamp (unix timestamp, in seconds) */
    @SerialName("created") val created: Long? = null,

    /**
     * Address for from_currency. this is the address user has to send crypto to in order to
     * initiate exchange. it can be set to "_GENERATING_" if address is not ready yet. for example,
     * "_GENERATING_" can happen if there's an error (state_error is set).
     */
    @SerialName("from_addr") val fromAddr: String,
    /** The currency the user has to send to "from_addr" */
    @SerialName("from_currency") val fromCurrency: String? = null,
    /** Amount of "from_currency" received (null when no amount received yet) */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("from_amount_received")
    val fromAmountReceived: BigDecimal? = null,

    /** Maximum amount of from_currency to deposit */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("max_input")
    val maxInput: BigDecimal? = null,
    /** Minimum amount of from_currency to deposit */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("min_input")
    val minInput: BigDecimal? = null,

    /** Network fee (included in the calculated amount of to_currency) */
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("network_fee")
    val networkFee: BigDecimal? = null,

    /** Current rate */
    @Serializable(with = BigDecimalSerializer::class) @SerialName("rate") val rate: BigDecimal,
    /** Rate mode ("flat" or "dynamic") */
    @SerialName("rate_mode") val rateMode: String,
    /**
     * Current state of the exchange, possible values: CREATED, CANCELLED, AWAITING_INPUT,
     * CONFIRMING_INPUT, EXCHANGING, CONFIRMING_SEND, COMPLETE, REFUND_REQUEST, REFUND_PENDING,
     * CONFIRMING_REFUND, REFUNDED
     */
    @SerialName("state") val state: OrderState,
    /** Current error state, if any (only present on error). such as "TO_ADDRESS_INVALID" */
    @SerialName("state_error") val stateError: String? = null,
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
    @SerialName("transaction_id_sent") val transactionIdSent: String? = null
)
