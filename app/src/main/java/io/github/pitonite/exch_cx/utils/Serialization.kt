package io.github.pitonite.exch_cx.utils

import android.annotation.SuppressLint
import io.github.pitonite.exch_cx.model.api.OrderWalletPool
import io.github.pitonite.exch_cx.model.api.RateFeesObjectTransformer
import io.github.pitonite.exch_cx.model.api.SupportMessagesArrayTransformer
import io.github.pitonite.exch_cx.model.api.XmlRateFeesResponse
import io.github.pitonite.exch_cx.utils.codified.Codified
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.serialization.XML
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalSerializationApi::class)
val jsonFormat = Json {
  isLenient = true
  ignoreUnknownKeys = true
  decodeEnumsCaseInsensitive = true
  explicitNulls = false
  serializersModule = SerializersModule {
    contextual(RateFeesObjectTransformer)
    contextual(SupportMessagesArrayTransformer)
  }
}

val xmlFormat =  XML {
  autoPolymorphic = true
  repairNamespaces = true
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

object BigDecimalSerializer : KSerializer<BigDecimal> {
  override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BigDecimal {
    return decoder.decodeString().toBigDecimal()
  }

  override fun serialize(encoder: Encoder, value: BigDecimal) {
    return encoder.encodeString(value.toString())
  }
}

object NullableBigDecimalSerializer : KSerializer<BigDecimal?> {
  override val descriptor = PrimitiveSerialDescriptor("NullableBigDecimal", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BigDecimal? {
    return decoder.decodeString().toBigDecimalOrNull()
  }

  override fun serialize(encoder: Encoder, value: BigDecimal?) {
    return encoder.encodeString(value.toString())
  }
}

object DateSerializer : KSerializer<Date> {
  override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

  @SuppressLint("SimpleDateFormat")
  private val df: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")

  override fun serialize(encoder: Encoder, value: Date) {
    return encoder.encodeString(df.format(value))
  }

  override fun deserialize(decoder: Decoder): Date {
    return df.parse(decoder.decodeString()) ?: throw Error("cannot parse date in DateSerializer")
  }
}
