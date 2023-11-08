package io.github.pitonite.exch_cx.model.api

import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import javax.annotation.concurrent.Immutable

@Serializable
enum class AggregationOption {
  @SerialName("yes") YES,
  @SerialName("no") NO,
  @SerialName("any") ANY,
}

@Serializable
@Immutable
data class OrderCreateRequest(
    /** Should be all upper case */
    @SerialName("from_currency") val fromCurrency: String,
    /** Should be all upper case */
    @SerialName("to_currency") val toCurrency: String,
    @SerialName("to_address") val toAddress: String,
    @SerialName("refund_address") val refundAddress: String? = null,
    @SerialName("fee_option") val feeOption: NetworkFeeOption? = null,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("from_amount")
    val calculatedFromAmount: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("to_amount")
    val calculatedToAmount: BigDecimal? = null,
    @SerialName("rate_mode") val rateMode: RateFeeMode,
    /** Referrer ID */
    @SerialName("ref") val referrerId: String? = null,
    @SerialName("aggregation") val aggregation: AggregationOption? = null,
)
