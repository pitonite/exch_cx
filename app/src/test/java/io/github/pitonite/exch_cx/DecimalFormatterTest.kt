package io.github.pitonite.exch_cx

import io.github.pitonite.exch_cx.utils.DecimalFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.DecimalFormatSymbols
import java.util.Locale

class DecimalFormatterTest {

  private val subject = DecimalFormatter(symbols = DecimalFormatSymbols(Locale.US))

  @Test
  fun `test cleanup decimal without fraction`() {
    val inputs = arrayOf("1", "123", "123131231", "3423423")
    for (input in inputs) {
      val result = subject.cleanup(input)
      assertEquals(input, result)
    }
  }

  @Test
  fun `test cleanup decimal with fraction normal case`() {
    val inputs = arrayOf("1.00", "123.1", "1231.31231", "3.423423")

    for (input in inputs) {
      val result = subject.cleanup(input)
      assertEquals(input, result)
    }
  }

  @Test
  fun `test cleanup decimal with fraction irregular inputs`() {
    val inputs =
        arrayOf(
            Pair("1231.12312.12312.", "1231.1231212312"),
            Pair("1.12312..", "1.12312"),
            Pair("...12..31.12312.123..12.", "12.311231212312"),
            Pair("---1231.-.-123-12.1-2312.", "1231.1231212312"),
            Pair("-.--1231.-.-123-12.1-2312.", "1231.1231212312"),
            Pair("....", ""),
            Pair(".-.-..-", ""),
            Pair("---", ""),
            Pair(".", ""),
            Pair("      ", ""),
            Pair("     1231.  -   12312.   -   12312.", "1231.1231212312"),
            Pair("1231.  -   12312.   -   12312.     ", "1231.1231212312"))

    for ((input, expected) in inputs) {
      val result = subject.cleanup(input)
      assertEquals(expected, result)
    }
  }
}
