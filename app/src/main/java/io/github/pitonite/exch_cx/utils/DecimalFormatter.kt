package io.github.pitonite.exch_cx.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormatSymbols

// from
// https://dev.to/tuvakov/decimal-input-formatting-with-jetpack-composes-visualtransformation-110n
// with edits

class DecimalFormatter(symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()) {
  private val decimalSeparator = symbols.decimalSeparator

  fun cleanup(input: String): String {

    if (input.matches("\\D".toRegex())) return ""
    if (input.matches("0+".toRegex())) return "0"

    val sb = StringBuilder()

    var hasDecimalSep = false

    for (char in input) {
      if (char.isDigit()) {
        sb.append(char)
        continue
      }
      if (char == decimalSeparator && !hasDecimalSep && sb.isNotEmpty()) {
        sb.append(char)
        hasDecimalSep = true
      }
    }

    return sb.toString()
  }
}

class DecimalInputVisualTransformation(private val decimalFormatter: DecimalFormatter) :
    VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {

    val inputText = text.text
    val formattedNumber = decimalFormatter.cleanup(inputText)

    val newText =
        AnnotatedString(
            text = formattedNumber,
            spanStyles = text.spanStyles,
            paragraphStyles = text.paragraphStyles)

    return TransformedText(newText, OffsetMapping.Identity)
  }
}
