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
    val letterOfGuaranteeExample = // this is an invalid letter of guarantee, just for preview purpose
        """
        -----BEGIN BITCOIN SIGNED MESSAGE-----
        This is a proof of the exchange order ee902b8a5fe0844d41 created at eXch (https://exch.cx) on Thu Nov 10 12:00:00 2023 to receive user's XMR at 8ALMe88qDeM91pnxtarFj6eqVwTLj5GRkjJ4xsS1MyXH3Kjb15gVgEK3drrkRP6ndrTYuygvXRR3aEToshyviJGNS2DqS3N and send ETH to 0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ. This message was generated on Thu Nov 10 12:00:00 2023
        -----BEGIN SIGNATURE-----
        1P59vP5TNXAFRnQJvZSkwhYXejYW3teXch
        HNM+up+wYiVL/MnZqTPFzOjS8EjhFyAUPy86ULLgI8E+LkyE9yNsthv2dAJ6ROoE8JWJozI7liLFT2Faw/Y+5gg=
        -----END BITCOIN SIGNED MESSAGE-----
      """.trimIndent()
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
        letterOfGuarantee = letterOfGuaranteeExample,
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
        letterOfGuarantee = letterOfGuaranteeExample,
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
        letterOfGuarantee = letterOfGuaranteeExample,
    )

    val orderAwaitingInput = Order(
        id = "ee902b8a5fe0844d41",
        fromCurrency = "eth",
        toCurrency = "btc",
        rate = BigDecimal.valueOf(18.867924528301927),
        rateMode = RateFeeMode.DYNAMIC,
        state = OrderState.AWAITING_INPUT.codifiedEnum(),
        svcFee = BigDecimal.valueOf(0.5),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        letterOfGuarantee = letterOfGuaranteeExample,
    )


    val orderAwaitingInputWithFromAmount = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "xmr",
        toCurrency = "eth",
        fromAddr = "zzzzsb5edVfZe7xZTPLarrZaDdg5UKYzs73ggY6jkuaFKjfbSPfmW15P72mG8K1CdiAwL4V5LLXGY98S1cctS2XNPhmUXGC",
        rate = "0.078380831366853865".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.0020280),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.AWAITING_INPUT.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        fromAmount = BigDecimal.ONE,
        letterOfGuarantee = letterOfGuaranteeExample,
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
        letterOfGuarantee = letterOfGuaranteeExample,
    )

    // note again, addresses here are randomly taken from internet, changed a bit, for demo purposes.
    val orderConfirmingInput = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "btc",
        toCurrency = "eth",
        fromAddr = "zzzzp3fvmc8yg0m0h9msh9gddgs2wlwzssstsrn2kr",
        rate = "0.078380831366853865".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.0020280),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.CONFIRMING_SEND.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        fromAmount = BigDecimal.ONE,
        letterOfGuarantee = letterOfGuaranteeExample,
        transactionIdReceived = "zzww481d0d50298eba3bf924354cfb77abf10399cc0704ac72a18d41037feb58",
        fromAmountReceived = "0.078380831366853865".toBigDecimal(),
    )

    val orderConfirmingInputEthNote = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "eth",
        toCurrency = "btc",
        fromAddr = "zzzzp3fvmc8yg0m0h9msh9gddgs2wlwzssstsrn2kr",
        rate = "0.078380831366853865".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.0020280),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.CONFIRMING_SEND.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        fromAmount = BigDecimal.ONE,
        letterOfGuarantee = letterOfGuaranteeExample,
        transactionIdReceived = null,
        fromAmountReceived = "0.078380831366853865".toBigDecimal(),
    )

    val orderExchanging = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "btc",
        toCurrency = "eth",
        fromAddr = "zzzzp3fvmc8yg0m0h9msh9gddgs2wlwzssstsrn2kr",
        rate = "0.078380831366853865".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.0020280),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.EXCHANGING.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        fromAmount = BigDecimal.ONE,
        letterOfGuarantee = letterOfGuaranteeExample,
        transactionIdReceived = "zzww481d0d50298eba3bf924354cfb77abf10399cc0704ac72a18d41037feb58",
        transactionIdSent = null,
        fromAmountReceived = "0.078380831366853865".toBigDecimal(),
    )

    val orderConfirmingSend = Order(
        id = "c6b728fe153f566d97",
        fromCurrency = "btc",
        toCurrency = "eth",
        fromAddr = "zzzzp3fvmc8yg0m0h9msh9gddgs2wlwzssstsrn2kr",
        rate = "0.078380831366853865".toBigDecimal(),
        networkFee = BigDecimal.valueOf(0.0020280),
        rateMode = RateFeeMode.FLAT,
        state = OrderState.CONFIRMING_SEND.codifiedEnum(),
        svcFee = BigDecimal.valueOf(1),
        toAddress = "0xZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ",
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
        fromAmount = BigDecimal.ONE,
        letterOfGuarantee = letterOfGuaranteeExample,
        transactionIdReceived = "zzww481d0d50298eba3bf924354cfb77abf10399cc0704ac72a18d41037feb58",
        transactionIdSent = "0xzzcb5154bbb0d9d8b6919e989d65f87ca4b536bf6b80f4c9a2b0de25ff26fd70",
        fromAmountReceived = "0.078380831366853865".toBigDecimal(),
        toAmount = "1.078380831366853865".toBigDecimal(),
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

  override fun getOrder(orderid: String): Flow<Order> {
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

  override suspend fun fetchOrder(orderid: String): OrderUpdate {
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

  override suspend fun fetchAndUpdateOrder(orderid: String): Boolean {
    return false
  }

  override suspend fun createOrder(createRequest: OrderCreateRequest): String {
    throw Error("Not implemented")
  }

  override suspend fun setArchive(orderid: String, value: Boolean) {
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
