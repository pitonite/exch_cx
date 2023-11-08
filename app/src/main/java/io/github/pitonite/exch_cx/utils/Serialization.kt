package io.github.pitonite.exch_cx.utils

import io.github.pitonite.exch_cx.model.api.RateFeesObjectTransformer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.serializer
import java.math.BigDecimal

@OptIn(ExperimentalSerializationApi::class)
val jsonFormat = Json {
  isLenient = true
  ignoreUnknownKeys = true
  decodeEnumsCaseInsensitive = true
  explicitNulls = false
  serializersModule = SerializersModule { contextual(RateFeesObjectTransformer) }
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
  override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BigDecimal {
    return decoder.decodeString().toBigDecimal()
  }

  override fun serialize(encoder: Encoder, value: BigDecimal) {
    encoder.encodeString(value.toString())
  }
}

inline fun <reified T> T.toParameterMap(): Map<String, String> {
  val jsonObject = jsonFormat.encodeToJsonElement(serializer(), this).jsonObject

  val map = mutableMapOf<String, String>()
  for ((key, value) in jsonObject) {
    if (value is JsonPrimitive && !value.contentOrNull.isNullOrBlank()) {
      map[key] = value.content
    }
  }
  return map
}
