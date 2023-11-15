package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateEntity
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.data.room.OrderUpdateWithArchive
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.OrderStateError
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum
import java.math.BigDecimal
import java.util.Date
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Stable
class OrderRepositoryMock : OrderRepository {

  companion object {
    val orders =
        persistentListOf(
            Order(
                id = "ee902b8a5fe0844d41",
                fromCurrency = "eth",
                toCurrency = "btc",
                rate = BigDecimal.valueOf(18.867924528301927),
                rateMode = RateFeeMode.DYNAMIC,
                state = OrderState.CREATED.codifiedEnum(),
                svcFee = BigDecimal.valueOf(0.5),
                toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
                minInput = BigDecimal.ZERO,
                maxInput = BigDecimal.ONE,
            ),
            Order(
                id = "ee902b8a5fe0844d41",
                fromCurrency = "eth",
                toCurrency = "btc",
                rate = BigDecimal.valueOf(18.867924528301927),
                rateMode = RateFeeMode.DYNAMIC,
                state = OrderState.CREATED.codifiedEnum(),
                svcFee = BigDecimal.valueOf(0.5),
                toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
                minInput = BigDecimal.ZERO,
                maxInput = BigDecimal.ZERO,
            ),
            Order(
                id = "ee902b8a5fe0844d41",
                fromCurrency = "BTC",
                toCurrency = "ETH",
                rate = BigDecimal.valueOf(18.867924528301927),
                rateMode = RateFeeMode.DYNAMIC,
                state = OrderState.CREATED.codifiedEnum(),
                svcFee = BigDecimal.valueOf(0.5),
                toAddress = "foo_address",
                stateError = OrderStateError.TO_ADDRESS_INVALID.codifiedEnum(),
                minInput = BigDecimal.ZERO,
                maxInput = BigDecimal.ONE,
            ),
            Order(
                id = "ee902b8a5fe0844d41",
                fromCurrency = "BTC",
                toCurrency = "ETH",
                rate = BigDecimal.valueOf(18.867924528301927),
                rateMode = RateFeeMode.FLAT,
                state = OrderState.CANCELLED.codifiedEnum(),
                svcFee = BigDecimal.valueOf(1),
                toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
                minInput = BigDecimal.ZERO,
                maxInput = BigDecimal.ONE,
            ),
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

  override suspend fun fetchOrder(orderId: String): OrderUpdate {
    return orders[0].toOrderUpdateEntity()
  }

  override suspend fun updateOrder(orderUpdate: Order): Boolean {
    return false
  }

  override suspend fun updateOrder(orderUpdate: OrderUpdate): Boolean {
    return false
  }

  override suspend fun updateOrder(orderUpdate: OrderUpdateWithArchive): Boolean {
    return false
  }

  override suspend fun fetchAndUpdateOrder(orderId: String): Boolean {
    return false
  }

  override suspend fun createOrder(createRequest: OrderCreateRequest): String {
    throw Error("Not implemented")
  }

  override suspend fun setArchive(orderId: String, value: Boolean) {
    throw Error("Not implemented")
  }

  override suspend fun count(archived: Boolean): Int {
    return orders.count()
  }

  override suspend fun deleteRemote(orderid: String) {}

  override suspend fun deleteLocal(orderid: String) {}
}
