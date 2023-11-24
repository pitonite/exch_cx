package io.github.pitonite.exch_cx.utils

object NumberFormatter {
  fun cleanup(input: String): String {

    if (input.matches("[^0-9]".toRegex())) return ""
    if (input.matches("0+".toRegex())) return "0"

    val sb = StringBuilder()

    for (char in input) {
      if (char.isDigit()) {
        sb.append(char)
        continue
      }
    }

    return sb.toString()
  }
}
