package io.github.pitonite.exch_cx.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.pitonite.exch_cx.data.room.typeconverters.BigDecimalTypeConverters
import io.github.pitonite.exch_cx.data.room.typeconverters.CodifiedOrderStateErrorTypeConverter
import io.github.pitonite.exch_cx.data.room.typeconverters.CodifiedOrderStateTypeConverter
import io.github.pitonite.exch_cx.data.room.typeconverters.CodifiedOrderWalletPoolConverter
import io.github.pitonite.exch_cx.data.room.typeconverters.DateTimeTypeConverters

/** The [RoomDatabase] we use in this app. */
@Database(
    entities =
        [
            Order::class,
            SupportMessage::class,
            CurrencyReserve::class,
            CurrencyReserveTrigger::class,
        ],
    autoMigrations =
        [
            AutoMigration(from = 1, to = 2),
        ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(
    DateTimeTypeConverters::class,
    BigDecimalTypeConverters::class,
    CodifiedOrderStateTypeConverter::class,
    CodifiedOrderStateErrorTypeConverter::class,
    CodifiedOrderWalletPoolConverter::class,
)
abstract class ExchDatabase : RoomDatabase() {
  abstract fun ordersDao(): OrderDao

  abstract fun supportMessagesDao(): SupportMessageDao

  abstract fun currencyReserveDao(): CurrencyReserveDao

  abstract fun currencyReserveTriggerDao(): CurrencyReserveTriggerDao
}
