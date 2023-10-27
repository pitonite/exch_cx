package io.github.pitonite.exch_cx.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.pitonite.exch_cx.data.room.typeconverters.BigDecimalTypeConverters
import io.github.pitonite.exch_cx.data.room.typeconverters.DateTimeTypeConverters

/** The [RoomDatabase] we use in this app. */
@Database(
    entities =
        [
            Order::class,
        ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    DateTimeTypeConverters::class,
    BigDecimalTypeConverters::class,
)
abstract class ExchDatabase : RoomDatabase() {
  abstract fun ordersDao(): OrderDao
}