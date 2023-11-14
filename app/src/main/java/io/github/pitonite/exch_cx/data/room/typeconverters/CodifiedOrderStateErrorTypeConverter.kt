package io.github.pitonite.exch_cx.data.room.typeconverters

import androidx.room.TypeConverter
import io.github.pitonite.exch_cx.model.api.OrderStateError
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum

object CodifiedOrderStateErrorTypeConverter {
  @TypeConverter
  @JvmStatic
  fun fromCodifiedEnum(value: CodifiedEnum<OrderStateError, String>?): String? {
    return value?.code()
  }

  @TypeConverter
  @JvmStatic
  fun toCodifiedEnum(value: String?): CodifiedEnum<OrderStateError, String>? {
    return value?.codifiedEnum()
  }
}
