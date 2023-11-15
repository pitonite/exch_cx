package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.data.room.OrderUpdateWithArchive
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Stable
interface OrderRepository {

  /** Loads an order from database */
  fun getOrder(orderId: String): Flow<Order?>

  fun getOrderAfter(createdAt: Date, archived: Boolean = false): Order?

  fun getOrderList(archived: Boolean, pageSize: Int = 10): Flow<PagingData<Order>>

  /** Returns order details from api. can throw errors. */
  suspend fun fetchOrder(orderId: String): OrderUpdate

  /** Returns true if the orderId already existed in db. */
  suspend fun updateOrder(orderUpdate: Order): Boolean

  /** Returns true if the orderId already existed in db. */
  suspend fun updateOrder(orderUpdate: OrderUpdate): Boolean

  /** Returns true if the orderId already existed in db. */
  suspend fun updateOrder(orderUpdate: OrderUpdateWithArchive): Boolean

  /** Returns true if the orderId already existed in db. */
  suspend fun fetchAndUpdateOrder(orderId: String): Boolean

  /** Returns orderid if successful */
  suspend fun createOrder(createRequest: OrderCreateRequest): String

  suspend fun setArchive(orderId: String, value: Boolean)

  suspend fun count(archived: Boolean): Int

  /** Requests to remove the order's data immediately (only when the order's state is COMPLETE) */
  suspend fun deleteRemote(orderid: String)

  /** deletes the user's local order data and support messages related to it */
  suspend fun deleteLocal(orderid: String)
}
