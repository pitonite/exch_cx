package io.github.pitonite.exch_cx.data

import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
  fun getOrderList(archived: Boolean): Flow<PagingData<Order>>

  /** Returns order details from api. can throw errors. */
  suspend fun fetchOrder(orderId: String): Order

  /** Returns true if the orderId already existed in db. */
  suspend fun updateOrder(order: OrderUpdate): Boolean

  /** Returns true if the orderId already existed in db. */
  suspend fun fetchAndUpdateOrder(orderId: String): Boolean
}
