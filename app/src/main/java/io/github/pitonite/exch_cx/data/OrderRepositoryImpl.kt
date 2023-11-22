package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateEntity
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderArchive
import io.github.pitonite.exch_cx.data.room.OrderCreate
import io.github.pitonite.exch_cx.data.room.OrderDeletedInRemote
import io.github.pitonite.exch_cx.data.room.OrderLetterOfGuarantee
import io.github.pitonite.exch_cx.data.room.OrderRefundAddress
import io.github.pitonite.exch_cx.data.room.OrderToAddress
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.data.room.OrderUpdateWithArchive
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.api.BooleanResult
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.OrderCreateResponse
import io.github.pitonite.exch_cx.model.api.OrderResponse
import io.github.pitonite.exch_cx.model.api.exceptions.FailedToConfirmRefundRequestException
import io.github.pitonite.exch_cx.model.api.exceptions.FailedToDeleteOrderDataException
import io.github.pitonite.exch_cx.model.api.exceptions.FailedToFetchLetterOfGuaranteeException
import io.github.pitonite.exch_cx.model.api.exceptions.FailedToRequestRefundException
import io.github.pitonite.exch_cx.model.api.exceptions.FailedToRevalidateToAddressException
import io.github.pitonite.exch_cx.model.api.exceptions.ToAddressRequiredException
import io.github.pitonite.exch_cx.utils.toParameterMap
import io.ktor.client.call.body
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
@Stable
class OrderRepositoryImpl
@Inject
constructor(
    private val exchDatabase: ExchDatabase,
    private val exchHttpClient: ExchHttpClient,
    private val userSettingsRepository: UserSettingsRepository,
    private val exchWorkManager: ExchWorkManager,
) : OrderRepository {
  override fun getOrder(orderid: String): Flow<Order?> {
    return exchDatabase.ordersDao().orderWithId(orderid)
  }

  override fun getOrderAfter(createdAt: Date, archived: Boolean): Order? {
    return exchDatabase.ordersDao().orderAfter(createdAt, archived)
  }

  override fun getOrderList(archived: Boolean, pageSize: Int): Flow<PagingData<Order>> {
    return Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = {
              exchDatabase.ordersDao().ordersSortedByCreatedAtPagingSource(archived = archived)
            },
        )
        .flow
  }

  override suspend fun fetchOrder(orderid: String): OrderUpdate {
    val orderResp: OrderResponse =
        exchHttpClient.get("/api/order/") { url { parameters.append("orderid", orderid) } }.body()

    return orderResp.toOrderUpdateEntity()
  }

  override suspend fun updateOrder(orderUpdate: Order): Boolean {
    val existedBeforeUpsert = exchDatabase.ordersDao().exists(orderUpdate.id)
    exchDatabase.ordersDao().upsert(orderUpdate)
    return existedBeforeUpsert
  }

  override suspend fun updateOrder(orderUpdate: OrderUpdate): Boolean {
    val existedBeforeUpsert = exchDatabase.ordersDao().exists(orderUpdate.id)
    exchDatabase.ordersDao().upsert(orderUpdate)
    return existedBeforeUpsert
  }

  override suspend fun updateOrder(orderUpdate: OrderUpdateWithArchive): Boolean {
    val existedBeforeUpsert = exchDatabase.ordersDao().exists(orderUpdate.id)
    exchDatabase.ordersDao().upsert(orderUpdate)
    return existedBeforeUpsert
  }

  /** Returns true if the orderid already existed in db. */
  override suspend fun fetchAndUpdateOrder(orderid: String): Boolean {
    val orderUpdate = this.fetchOrder(orderid)
    return this.updateOrder(orderUpdate)
  }

  override suspend fun createOrder(createRequest: OrderCreateRequest): String {
    val requestParams = createRequest.toParameterMap()

    try {
      val resp: OrderCreateResponse =
          exchHttpClient
              .get("/api/create") {
                url {
                  requestParams.forEach { (key, value) ->
                    parameters.append(
                        key,
                        if (key == "from_currency" || key == "to_currency") value.uppercase()
                        else value)
                  }
                }
              }
              .body()

      val orderCreate =
          OrderCreate(
              id = resp.orderid,
              fromCurrency = createRequest.fromCurrency,
              toCurrency = createRequest.toCurrency,
              networkFee = createRequest.networkFee,
              rate = createRequest.rate,
              rateMode = createRequest.rateMode,
              svcFee = createRequest.svcFee,
              toAddress = createRequest.toAddress,
              refundAddress = createRequest.refundAddress,
              minInput = BigDecimal.ZERO,
              maxInput = BigDecimal.ONE,
              fromAmount = createRequest.fromAmount,
          )

      exchDatabase.ordersDao().upsert(orderCreate)

      val userSettings = userSettingsRepository.fetchSettings()
      exchWorkManager.adjustAutoUpdater(
          userSettings) // start the auto update again if user settings allows

      return resp.orderid
    } catch (e: Throwable) {
      val error = e.message ?: e.toString()
      if (error.contains("address is required")) {
        throw ToAddressRequiredException()
      }
      throw RuntimeException(error)
    }
  }

  override suspend fun setArchive(orderid: String, value: Boolean) {
    exchDatabase.ordersDao().setArchive(OrderArchive(orderid, value))
  }

  override suspend fun count(archived: Boolean): Int {
    return exchDatabase.ordersDao().count(archived)
  }

  override suspend fun revalidateAddress(orderid: String, newToAddress: String) {
    val resp: BooleanResult =
        exchHttpClient
            .get("/api/order/revalidate_address") {
              url {
                parameters.append("orderid", orderid)
                parameters.append("to_address", newToAddress)
              }
            }
            .body()

    if (!resp.result) throw FailedToRevalidateToAddressException()

    exchDatabase.ordersDao().setToAddress(OrderToAddress(orderid, newToAddress))
  }

  override suspend fun requestRefund(orderid: String) {
    val resp: BooleanResult =
        exchHttpClient
            .get("/api/order/refund") { url { parameters.append("orderid", orderid) } }
            .body()

    if (!resp.result) throw FailedToRequestRefundException()
  }

  override suspend fun requestRefundConfirm(orderid: String, refundAddress: String) {
    exchDatabase.ordersDao().setRefundAddress(OrderRefundAddress(orderid, refundAddress))

    val resp: BooleanResult =
        exchHttpClient
            .get("/api/order/refund_confirm") {
              url {
                parameters.append("orderid", orderid)
                parameters.append("refund_address", refundAddress)
              }
            }
            .body()

    if (!resp.result) throw FailedToConfirmRefundRequestException()
  }

  override suspend fun fetchAndUpdateLetterOfGuarantee(orderid: String) {
    val resp: String =
        exchHttpClient
            .get("/api/order/fetch_guarantee") { url { parameters.append("orderid", orderid) } }
            .body()

    if (resp.isNullOrEmpty()) throw FailedToFetchLetterOfGuaranteeException()

    exchDatabase
        .ordersDao()
        .setLetterOfGuarantee(OrderLetterOfGuarantee(id = orderid, letterOfGuarantee = resp))
  }

  override suspend fun deleteRemote(orderid: String) {
    val resp: BooleanResult =
        exchHttpClient
            .get("/api/order/remove") { url { parameters.append("orderid", orderid) } }
            .body()

    if (!resp.result) throw FailedToDeleteOrderDataException()

    exchDatabase.ordersDao().setDeletedInRemote(OrderDeletedInRemote(orderid, true))
  }

  override suspend fun deleteLocal(orderid: String) {
    exchDatabase.ordersDao().delete(orderid)
  }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderRepositoryModule {
  @Binds
  @Singleton
  abstract fun provideOrderRepositoryModule(
      orderRepositoryImpl: OrderRepositoryImpl
  ): OrderRepository
}
