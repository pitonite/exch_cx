package io.github.pitonite.exch_cx.exceptions

import androidx.annotation.StringRes
import io.github.pitonite.exch_cx.model.Translatable
import io.github.pitonite.exch_cx.model.UserMessage
import javax.annotation.concurrent.Immutable

@Immutable
open class LocalizedException(@StringRes override val translation: Int) : Exception(), Translatable

fun Throwable.toUserMessage(): UserMessage {
  return if (this is LocalizedException) UserMessage.from(this.translation)
  else UserMessage.from(this.localizedMessage ?: this.message ?: this.toString())
}
