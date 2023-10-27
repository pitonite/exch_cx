package io.github.pitonite.exch_cx.data

import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
  fun getOrderList(archived: Boolean): Flow<PagingData<Order>>

  /** Returns true if the orderId already existed in db. */
  suspend fun updateOrder(orderId: String): Boolean
}
