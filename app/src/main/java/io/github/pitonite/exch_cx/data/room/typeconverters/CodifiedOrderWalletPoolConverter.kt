package io.github.pitonite.exch_cx.data.room.typeconverters

import androidx.room.TypeConverter
import io.github.pitonite.exch_cx.model.api.OrderWalletPool
import io.github.pitonite.exch_cx.utils.codified.enums.CodifiedEnum
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum

object CodifiedOrderWalletPoolConverter {
  @TypeConverter
  @JvmStatic
  fun fromCodifiedEnum(value: CodifiedEnum<OrderWalletPool, String>?): String? {
    return value?.code()
  }

  @TypeConverter
  @JvmStatic
  fun toCodifiedEnum(value: String?): CodifiedEnum<OrderWalletPool, String>? {
    return value?.codifiedEnum()
  }
}
