package io.github.pitonite.exch_cx.model.api

import androidx.compose.runtime.Immutable
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.math.BigDecimal

// for /api/rates

@Serializable
enum class NetworkFeeChoice {
  @SerialName("f") QUICK,
  @SerialName("m") MEDIUM,
  @SerialName("s") SLOW,
}

enum class RateFeeMode {
  @SerialName("flat") FLAT,
  @SerialName("dynamic") DYNAMIC
}

@Serializable
@Immutable
data class RateFee(
    @SerialName("from_currency") val fromCurrency: String, // doesn't exist on original response
    @SerialName("to_currency") val toCurrency: String, // doesn't exist on original response
    @Serializable(with = BigDecimalSerializer::class) @SerialName("rate") val rate: BigDecimal,
    @SerialName("rate_mode") val rateMode: RateFeeMode,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("reserve")
    val reserve: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("svc_fee") val svcFee: BigDecimal,
    @SerialName("network_fee")
    val networkFee:
        Map<NetworkFeeChoice, @Serializable(with = BigDecimalSerializer::class) BigDecimal>? =
        null,
)

@Serializable
@Immutable
data class RateFeeResponse(
    val rateFees: List<RateFee>,
)

object RateFeesObjectTransformer :
    JsonTransformingSerializer<RateFeeResponse>(RateFeeResponse.serializer()) {

  override fun transformDeserialize(element: JsonElement): JsonElement {
    val rateFees =
        JsonArray(
            element.jsonObject.map { (k, v) ->
              buildJsonObject {
                val (fromCurrency, toCurrency) = k.split('_')
                put("from_currency", fromCurrency.lowercase())
                put("to_currency", toCurrency.lowercase())
                v.jsonObject.forEach { (k, v) -> put(k, v) }
              }
            })

    return buildJsonObject { put("rateFees", rateFees) }
  }
}
