package io.github.pitonite.exch_cx.data.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class CurrencyReserveDao : BaseDao<CurrencyReserve> {

  @Transaction
  open suspend fun upsertReserves(reserves: List<CurrencyReserve>) {
    reserves.forEach { upsert(it) }
  }

  @Query("SELECT * FROM `CurrencyReserve` ORDER BY currency")
  abstract suspend fun getAllReserves(): List<CurrencyReserve>
}
