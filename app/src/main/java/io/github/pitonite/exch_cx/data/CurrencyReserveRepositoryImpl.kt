package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.room.CurrencyReserve
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.model.api.RateFeeResponse
import io.github.pitonite.exch_cx.network.ExchHttpClient
import io.ktor.client.call.body
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

@Singleton
@Stable
class CurrencyReserveRepositoryImpl
@Inject
constructor(
  private val exchDatabase: ExchDatabase,
  private val httpClient: ExchHttpClient,
) : CurrencyReserveRepository {
  override suspend fun fetchAndUpdateReserves(): List<CurrencyReserve> {
    val rates = httpClient.get("/api/rates").body<RateFeeResponse>().rateFees
    val reserves = mutableMapOf<String, CurrencyReserve>()
    rates.forEach {
      reserves[it.toCurrency.lowercase()] =
          CurrencyReserve(
              currency = it.toCurrency.lowercase(),
              amount = it.reserve,
          )
    }

    val reservesList = reserves.values.toList().sortedBy { it.currency }
    exchDatabase.currencyReserveDao().upsertReserves(reservesList)

    return reservesList
  }

  override suspend fun getCurrencyReserves(): List<CurrencyReserve> {
    return exchDatabase.currencyReserveDao().getAllReserves()
  }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyReserveRepositoryModule {
  @Binds
  @Singleton
  abstract fun provideCurrencyReserveRepositoryModule(
    currencyReserveRepositoryImpl: CurrencyReserveRepositoryImpl
  ): CurrencyReserveRepository
}
