package io.github.pitonite.exch_cx.exceptions

import androidx.annotation.StringRes
import javax.annotation.concurrent.Immutable

@Immutable
open class LocalizedException(@StringRes val msgId: Int) : Exception()
