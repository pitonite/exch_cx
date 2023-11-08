package io.github.pitonite.exch_cx.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.data.mappers.toOrderEntity
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateEntity
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderArchive
import io.github.pitonite.exch_cx.data.room.OrderCreate
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.OrderResponse
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.utils.ExchParser
import io.github.pitonite.exch_cx.utils.toParameterMap
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl
@Inject
constructor(
    private val exchDatabase: ExchDatabase,
    private val exchHttpClient: ExchHttpClient,
    private val userSettingsRepository: UserSettingsRepository,
    private val exchWorkManager: ExchWorkManager,
    @ApplicationContext private val context: Context,
) : OrderRepository {
  override fun getOrder(orderId: String): Flow<Order?> {
    return exchDatabase.ordersDao().orderWithId(orderId)
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

  override suspend fun fetchOrder(orderId: String): Order {
    val orderResp: OrderResponse =
        exchHttpClient.get("/api/order/") { url { parameters.append("orderid", orderId) } }.body()
    var order = orderResp.toOrderEntity()

    // TODO: remove duplicate call when api is fixed
    val resp = exchHttpClient.get("/order/$orderId") { headers["X-Requested-With"] = "" }
    if (resp.status != HttpStatusCode.OK || resp.call.request.url.fullPath == "/") {
      throw RuntimeException("orderid not found")
    }
    val body = resp.bodyAsText()
    val parsedOrder =
        ExchParser.parseOrder(body) ?: throw RuntimeException("order parsing had a problem")

    order =
        order
            .copy(
                rate = parsedOrder.rate,
                calculatedFromAmount = parsedOrder.calculatedFromAmount,
                calculatedToAmount = parsedOrder.calculatedToAmount,
            )
            .let {
              if (parsedOrder.networkFee != null) {
                return@let it.copy(networkFee = parsedOrder.networkFee)
              }
              return@let it
            }
    // end of duplicate call

    return order
  }

  override suspend fun updateOrder(orderUpdate: OrderUpdate): Boolean {
    val existedBeforeUpsert = exchDatabase.ordersDao().exists(orderUpdate.id)
    exchDatabase.ordersDao().upsert(orderUpdate)
    return existedBeforeUpsert
  }

  override suspend fun fetchAndUpdateOrder(orderId: String): Boolean {
    val orderUpdate = this.fetchOrder(orderId).toOrderUpdateEntity()
    return this.updateOrder(orderUpdate)
  }

  override suspend fun createOrder(createRequest: OrderCreateRequest, rate: RateFee): String {
    val requestParams = createRequest.toParameterMap()

    val resp =
        exchHttpClient.get("/") {
          url {
            headers["X-Requested-With"] = ""
            requestParams.forEach { (key, value) ->
              parameters.append(
                  key,
                  if (key == "from_currency" || key == "to_currency") value.uppercase() else value)
            }
            parameters.append("create", "1")

            if (parameters["from_amount"].isNullOrEmpty()) {
              parameters["from_amount"] = "1"
            }
          }
        }

    if (resp.status != HttpStatusCode.OK) {
      throw RuntimeException("service unavailable")
    }

    val htmlBody = resp.bodyAsText()
    val error = ExchParser.parseError(htmlBody)
    if (error != null) {
      throw RuntimeException(error)
    }

    val createdOrder =
        ExchParser.parseOrder(htmlBody)
            ?: throw RuntimeException("Failed to retrieve created order.")

    val orderCreate =
        OrderCreate(
            id = createdOrder.orderid,
            fromCurrency = createRequest.fromCurrency,
            toCurrency = createRequest.toCurrency,
            networkFee = createdOrder.networkFee,
            rate = createdOrder.rate,
            rateMode = createRequest.rateMode,
            svcFee = rate.svcFee,
            toAddress = createRequest.toAddress,
            calculatedFromAmount = createdOrder.calculatedFromAmount,
            calculatedToAmount = createdOrder.calculatedToAmount,
            refundAddress = createRequest.refundAddress,
        )

    exchDatabase.ordersDao().upsert(orderCreate)

    val userSettings = userSettingsRepository.fetchSettings()
    exchWorkManager.adjustAutoUpdater(
        userSettings) // start the auto update again if user settings allows

    return createdOrder.orderid
  }

  override suspend fun setArchive(orderId: String, value: Boolean) {
    exchDatabase.ordersDao().setArchive(OrderArchive(orderId, value))
  }

  override suspend fun count(archived: Boolean): Int {
    return exchDatabase.ordersDao().count(archived)
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
