package io.github.pitonite.exch_cx.data

import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.RateFee
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface OrderRepository {

  /** Loads an order from database */
  fun getOrder(orderId: String): Flow<Order?>

  fun getOrderAfter(createdAt: Date, archived: Boolean = false): Order?

  fun getOrderList(archived: Boolean, pageSize: Int = 10): Flow<PagingData<Order>>

  /** Returns order details from api. can throw errors. */
  suspend fun fetchOrder(orderId: String): Order

  /** Returns true if the orderId already existed in db. */
  suspend fun updateOrder(orderUpdate: OrderUpdate): Boolean

  /** Returns true if the orderId already existed in db. */
  suspend fun fetchAndUpdateOrder(orderId: String): Boolean

  /** Returns orderid if successful */
  suspend fun createOrder(createRequest: OrderCreateRequest, rate: RateFee): String

  suspend fun setArchive(orderId: String, value: Boolean)

  suspend fun count(archived: Boolean): Int
}
