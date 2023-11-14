package io.github.pitonite.exch_cx.utils.codified.enums

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pitonite.exch_cx.model.Translatable
import io.github.pitonite.exch_cx.utils.capitalizeWords
import io.github.pitonite.exch_cx.utils.codified.Codified
import java.io.Serializable

// with some edits
sealed class CodifiedEnum<T, C> : Serializable where T : Enum<T>, T : Codified<C> {
  override fun equals(other: Any?): Boolean {
    return when (other) {
      is CodifiedEnum<*, *> -> {
        this.code() == other.code()
      }
      else -> super.equals(other)
    }
  }

  data class Known<T, C>(val value: T) : CodifiedEnum<T, C>() where T : Enum<T>, T : Codified<C>

  data class Unknown<T, C>(val value: C) : CodifiedEnum<T, C>() where T : Enum<T>, T : Codified<C>

  fun knownOrNull() = (this as? Known<T, C>)?.value

  fun code(): C =
      when (this) {
        is Known -> value.code
        is Unknown -> value
      }

  companion object {
    inline fun <reified T> decode(value: String): CodifiedEnum<T, String> where
    T : Enum<T>,
    T : Codified<String> {
      return CodifiedEnumDecoder.decode(value, T::class.java)
    }
  }
}

fun <T, C> T.codifiedEnum(): CodifiedEnum<T, C> where T : Codified<C>, T : Enum<T> =
    CodifiedEnum.Known(this)

inline fun <reified T> String.codifiedEnum(): CodifiedEnum<T, String> where
T : Enum<T>,
T : Codified<String> = CodifiedEnum.decode(this)

inline fun <reified T, C> codes(): List<C> where T : Codified<C>, T : Enum<T> =
    enumValues<T>().map(Codified<C>::code)

// added edits:

@Composable
fun CodifiedEnum<*, String>.toLocalizedString(): String {
  val value = this.knownOrNull()
  if (value is Translatable) {
    return value.translation?.let { stringResource(it) } ?: this.code().capitalizeWords()
  }
  return this.code().capitalizeWords()
}

fun CodifiedEnum<*, String>.toLocalizedString(context: Context): String {
  val value = this.knownOrNull()
  if (value is Translatable) {
    return value.translation?.let { context.getString(it) } ?: this.code().capitalizeWords()
  }
  return this.code().capitalizeWords()
}
