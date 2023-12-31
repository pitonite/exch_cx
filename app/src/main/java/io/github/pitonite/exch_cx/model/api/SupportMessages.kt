package io.github.pitonite.exch_cx.model.api

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.Translatable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray

@Serializable
@Stable
enum class SupportMessageSender(override val translation: Int? = null) : Translatable {
  @SerialName("user") USER(R.string.sender_user),
  @SerialName("support") SUPPORT(R.string.sender_support),
}

@Serializable
@Stable
data class SupportMessageResponse(
    @SerialName("message") val message: String,
    @SerialName("read_by_support") val readBySupport: Boolean = false,
    @SerialName("sender") val sender: SupportMessageSender,
    @SerialName("timestamp") val timestamp: Long,
)

@Serializable
@Stable
data class SupportMessagesResponse(
  val messages: List<SupportMessageResponse>,
)

object SupportMessagesArrayTransformer :
    JsonTransformingSerializer<SupportMessagesResponse>(SupportMessagesResponse.serializer()) {

  override fun transformDeserialize(element: JsonElement): JsonElement {

    return buildJsonObject { put("messages", element.jsonArray) }
  }
}

