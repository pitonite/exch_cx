package io.github.pitonite.exch_cx.data.room.typeconverters

import androidx.room.TypeConverter
import java.math.BigDecimal

object BigDecimalTypeConverters {
  @TypeConverter
  @JvmStatic
  fun fromBigDecimal(value: BigDecimal?): String? {
    return value?.let { value.toString() }
  }

  @TypeConverter
  @JvmStatic
  fun stringToBigDecimal(value: String?): BigDecimal? {
    return value?.toBigDecimalOrNull() ?: BigDecimal.ZERO
  }
}
