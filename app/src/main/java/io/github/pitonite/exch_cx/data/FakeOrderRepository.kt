package io.github.pitonite.exch_cx.data

import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal

class FakeOrderRepository : OrderRepository {
  override fun getOrderList(archived: Boolean): Flow<PagingData<Order>> {
    return flow {
      emit(
          PagingData.from(
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
              ),
          ),
      )
    }
  }

  override suspend fun updateOrder(orderId: String): Boolean {
    return false
  }
}
