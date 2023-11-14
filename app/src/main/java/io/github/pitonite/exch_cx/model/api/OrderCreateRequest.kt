package io.github.pitonite.exch_cx.model.api

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal

@Serializable
@Stable
enum class AggregationOption {
  @SerialName("yes") YES,
  @SerialName("no") NO,
  @SerialName("any") ANY,
}

@Serializable
@Stable
data class OrderCreateRequest(
    /** Should be all upper case */
    @SerialName("from_currency") val fromCurrency: String,
    /** Should be all upper case */
    @SerialName("to_currency") val toCurrency: String,
    @SerialName("to_address") val toAddress: String,
    @SerialName("refund_address") val refundAddress: String? = null,
    @SerialName("fee_option") val feeOption: NetworkFeeOption? = null,
    @SerialName("rate_mode") val rateMode: RateFeeMode,
    /** Referrer ID */
    @SerialName("ref") val referrerId: String? = null,
    @SerialName("aggregation") val aggregation: AggregationOption? = null,
    // remove transient and add to_amount if api supports later
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("from_amount")
    @Transient
    val fromAmount: BigDecimal? = null,

    // don't remove transient from these anyway:
    @Transient val rate: BigDecimal = BigDecimal.ZERO,
    @Transient val networkFee: BigDecimal = BigDecimal.ZERO,
    @Transient val svcFee: BigDecimal = BigDecimal.ZERO,
)

@Serializable
@Stable
data class OrderCreateResponse(
    @SerialName("orderid") val orderid: String,
)
