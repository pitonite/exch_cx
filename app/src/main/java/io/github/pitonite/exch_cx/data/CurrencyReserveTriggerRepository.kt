package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import kotlinx.coroutines.flow.Flow

@Stable
interface CurrencyReserveTriggerRepository {

  fun getTriggersFlow(pageSize: Int = 10): Flow<PagingData<CurrencyReserveTrigger>>

  suspend fun getActiveTriggers(): List<CurrencyReserveTrigger>

  suspend fun upsertTrigger(trigger: CurrencyReserveTrigger)

  suspend fun updateTrigger(trigger: CurrencyReserveTrigger)

  suspend fun deleteTrigger(id: Int)

  suspend fun count(isEnabled: Boolean?= null): Int
}
