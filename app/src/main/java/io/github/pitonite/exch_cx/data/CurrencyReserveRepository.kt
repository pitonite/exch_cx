package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.CurrencyReserve
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.data.room.OrderUpdateWithArchive
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Stable
interface CurrencyReserveRepository {

  suspend fun fetchAndUpdateReserves(): List<CurrencyReserve>
  suspend fun getCurrencyReserves(): List<CurrencyReserve>
}
