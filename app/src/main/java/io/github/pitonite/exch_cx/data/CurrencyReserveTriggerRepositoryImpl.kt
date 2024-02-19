package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
@Stable
class CurrencyReserveTriggerRepositoryImpl
@Inject
constructor(
    private val exchDatabase: ExchDatabase,
) : CurrencyReserveTriggerRepository {

  override fun getTriggersFlow(pageSize: Int): Flow<PagingData<CurrencyReserveTrigger>> {
    return Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = {
              exchDatabase.currencyReserveTriggerDao().triggersSortedPagingSource()
            },
        )
        .flow
  }

  override suspend fun getActiveTriggers(): List<CurrencyReserveTrigger> {
    return exchDatabase.currencyReserveTriggerDao().getActiveTriggers()
  }

  override suspend fun upsertTrigger(trigger: CurrencyReserveTrigger) {
    exchDatabase.currencyReserveTriggerDao().upsert(trigger)
  }

  override suspend fun updateTrigger(trigger: CurrencyReserveTrigger) {
    exchDatabase.currencyReserveTriggerDao().update(trigger)
  }

  override suspend fun deleteTrigger(id: Int) {
    exchDatabase.currencyReserveTriggerDao().delete(id)
  }

  override suspend fun count(isEnabled: Boolean?): Int {
    return if (isEnabled != null) {
      exchDatabase.currencyReserveTriggerDao().count(isEnabled)
    } else {
      exchDatabase.currencyReserveTriggerDao().count()
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyReserveTriggerRepositoryModule {
  @Binds
  @Singleton
  abstract fun provideCurrencyReserveTriggerRepositoryModule(
      currencyReserveTriggerRepositoryImpl: CurrencyReserveTriggerRepositoryImpl
  ): CurrencyReserveTriggerRepository
}
