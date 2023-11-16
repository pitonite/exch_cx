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
    val orderCancelled = Order(
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
    )

    val orderCreated = Order(
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
    )

    val orderCreatedToAddressInvalid = Order(
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
    )

    val orderAwaitingInput = Order(
        id = "ee902b8a5fe0844d41",
        fromCurrency = "eth",
        toCurrency = "btc",
        fromAddr = "89yfsb5edVfZe7xZTPLarrZaDdg5UKYzs73ggY6jkuaFKjfbSPfmW15P72mG8K1CdiAwL4V5LLXGY98S1cctS2XNPhmUXGC",
        rate = BigDecimal.valueOf(18.867924528301927),
        rateMode = RateFeeMode.DYNAMIC,
        state = OrderState.AWAITING_INPUT.codifiedEnum(),
        svcFee = BigDecimal.valueOf(0.5),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
    )


    val orderAwaitingInputWithFromAmount = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "xmr",
        toCurrency = "eth",
        fromAddr = "89yfsb5edVfZe7xZTPLarrZaDdg5UKYzs73ggY6jkuaFKjfbSPfmW15P72mG8K1CdiAwL4V5LLXGY98S1cctS2XNPhmUXGC",
        rate = "0.078380831366853865".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.0020280),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.AWAITING_INPUT.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        fromAmount = BigDecimal.ONE,
    )

    val orderAwaitingInputMaxInputZero = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "xmr",
        toCurrency = "eth",
        rate = "0.078180003629105696".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.00087897),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.AWAITING_INPUT.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ZERO,
    )

    val orders =
        persistentListOf(
            orderCancelled,
            orderCreated,
            orderCreatedToAddressInvalid,
            orderAwaitingInput,
            orderAwaitingInputWithFromAmount,
            orderAwaitingInputMaxInputZero,
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

  override suspend fun revalidateAddress(orderid: String, newToAddress: String) {
  }

  override suspend fun requestRefund(orderid: String) {
  }

  override suspend fun requestRefundConfirm(orderid: String, refundAddress: String) {
  }

  override suspend fun fetchAndUpdateLetterOfGuarantee(orderid: String) {
  }

  override suspend fun deleteRemote(orderid: String) {}

  override suspend fun deleteLocal(orderid: String) {}
}
