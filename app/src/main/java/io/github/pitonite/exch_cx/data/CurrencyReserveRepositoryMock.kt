package io.github.pitonite.exch_cx.data

import io.github.pitonite.exch_cx.data.room.CurrencyReserve
import java.math.BigDecimal
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class CurrencyReserveRepositoryMock : CurrencyReserveRepository {
  companion object {
    val currencyReserves =
        listOf(
            CurrencyReserve(currency = "btc", amount = BigDecimal.ONE),
            CurrencyReserve(currency = "eth", amount = BigDecimal.ZERO),
        )
  }

  override suspend fun fetchAndUpdateReserves(): List<CurrencyReserve> {
    return currencyReserves
  }

  override suspend fun getCurrencyReserves(): List<CurrencyReserve> {
    return currencyReserves
  }
}
