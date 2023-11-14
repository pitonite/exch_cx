package io.github.pitonite.exch_cx.utils

import java.util.Locale

fun String.capitalizeWords(): String {
  return this.lowercase().split('_').joinToString(" ") { w ->
    w.replaceFirstChar {
      if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
  }
}
