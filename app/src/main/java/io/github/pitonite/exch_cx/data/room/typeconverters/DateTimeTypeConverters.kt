package io.github.pitonite.exch_cx.data.room.typeconverters

import androidx.room.TypeConverter
import java.util.Date

object DateTimeTypeConverters {
  @TypeConverter
  @JvmStatic
  fun fromTimestamp(value: Long?): Date? {
    return value?.let { Date(it) }
  }

  @TypeConverter
  @JvmStatic
  fun dateToTimestamp(date: Date?): Long? {
    return date?.time
  }
}
