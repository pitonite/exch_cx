package io.github.pitonite.exch_cx.data.room.typeconverters

import androidx.room.TypeConverter
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum

object CodifiedOrderStateTypeConverter {

  @TypeConverter
  @JvmStatic
  fun fromCodifiedEnum(value: CodifiedEnum<OrderState, String>?): String? {
    return value?.code()
  }

  @TypeConverter
  @JvmStatic
  fun toCodifiedEnum(value: String?): CodifiedEnum<OrderState, String>? {
    return value?.codifiedEnum()
  }
}
