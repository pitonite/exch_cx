package io.github.pitonite.exch_cx.data

import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import java.util.Date

class OrderRepositoryMock : OrderRepository {

  companion object {
    val orders =
        persistentListOf(
            Order(
                id = "ee902b8a5fe0844d41",
                fromCurrency = "BTC",
                toCurrency = "ETH",
                rate = BigDecimal.valueOf(18.867924528301927),
                rateMode = RateFeeMode.DYNAMIC,
                state = OrderState.CREATED,
                svcFee = BigDecimal.valueOf(1),
                toAddress = "foo_address"),
        )
  }

  override fun getOrder(orderId: String): Flow<Order> {
    return flow { emit(orders[0]) }
  }

  override fun getOrderAfter(createdAt: Date, archived: Boolean): Order? {
    return null
  }

  override fun getOrderList(archived: Boolean, pageSize: Int): Flow<PagingData<Order>> {
    return flow {
      emit(
          PagingData.from(orders),
      )
    }
  }

  override suspend fun fetchOrder(orderId: String): Order {
    return Order(
        id = "ee902b8a5fe0844d41",
        fromAddr = "_GENERATING_",
        fromCurrency = "BTC",
        toCurrency = "ETH",
        rate = BigDecimal.valueOf(18.867924528301927),
        rateMode = RateFeeMode.DYNAMIC,
        state = OrderState.CREATED,
        svcFee = BigDecimal.valueOf(1),
        toAddress = "foo_address")
  }

  override suspend fun updateOrder(orderUpdate: OrderUpdate): Boolean {
    return false
  }

  override suspend fun fetchAndUpdateOrder(orderId: String): Boolean {
    return false
  }

  override suspend fun createOrder(createRequest: OrderCreateRequest, rate: RateFee): String {
    throw Error("Not implemented")
  }

  override suspend fun setArchive(orderId: String, value: Boolean) {
    throw Error("Not implemented")
  }

  override suspend fun count(archived: Boolean): Int {
    return orders.count()
  }
}
