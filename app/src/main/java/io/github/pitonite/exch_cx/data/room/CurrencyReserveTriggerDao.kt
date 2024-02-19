package io.github.pitonite.exch_cx.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CurrencyReserveTriggerDao : BaseDao<CurrencyReserveTrigger> {
  @Query("SELECT * FROM `CurrencyReserveTrigger` WHERE id = :id")
  abstract fun triggerById(id: Int): Flow<CurrencyReserveTrigger?>

  @Transaction
  @Query("SELECT * FROM `CurrencyReserveTrigger` ORDER BY `createdAt` ASC")
  abstract fun triggersSortedPagingSource(): PagingSource<Int, CurrencyReserveTrigger>

  @Query("SELECT * FROM `CurrencyReserveTrigger` WHERE `isEnabled` = 1 ORDER BY currency")
  abstract suspend fun getActiveTriggers(): List<CurrencyReserveTrigger>

  @Query("DELETE FROM `CurrencyReserveTrigger` WHERE id = :triggerId")
  abstract suspend fun delete(triggerId: Int)

  @Query("SELECT COUNT(*) FROM `CurrencyReserveTrigger` where isEnabled = :isEnabled")
  abstract suspend fun count(isEnabled: Boolean): Int

  @Query("SELECT COUNT(*) FROM `CurrencyReserveTrigger`")
  abstract suspend fun count(): Int
}
