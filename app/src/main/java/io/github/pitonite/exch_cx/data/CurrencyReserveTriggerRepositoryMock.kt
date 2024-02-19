package io.github.pitonite.exch_cx.data

import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CurrencyReserveTriggerRepositoryMock : CurrencyReserveTriggerRepository {
  override fun getTriggersFlow(pageSize: Int): Flow<PagingData<CurrencyReserveTrigger>> {
    return flow {
      emit(
          PagingData.from(emptyList()),
      )
    }
  }

  override suspend fun getActiveTriggers(): List<CurrencyReserveTrigger> {
    return listOf()
  }

  override suspend fun upsertTrigger(trigger: CurrencyReserveTrigger) {}

  override suspend fun updateTrigger(trigger: CurrencyReserveTrigger) {}

  override suspend fun deleteTrigger(id: Int) {}

  override suspend fun count(isEnabled: Boolean?): Int {
    return 0
  }
}
