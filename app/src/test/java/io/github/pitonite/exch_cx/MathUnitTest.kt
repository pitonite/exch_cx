package io.github.pitonite.exch_cx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MathUnitTest {

  @Test
  fun parsing_isCorrect() {
    val num = "0.000000000000000001".toBigDecimalOrNull()
    assertNotNull(num)
  }

  @Test
  fun addition_isCorrect() {
    val num1 = "0.000000000000000001".toBigDecimalOrNull()
    val num2 = "0.000000000000000003".toBigDecimalOrNull()
    assertEquals("0.000000000000000004".toBigDecimalOrNull(), num1?.add(num2))
  }

  @Test
  fun subtraction_isCorrect() {
    val num1 = "0.000000000000000001".toBigDecimalOrNull()
    val num2 = "0.000000000000000001".toBigDecimalOrNull()
    assertEquals("0.000000000000000000".toBigDecimalOrNull(), num1?.subtract(num2))
  }
}
