package io.github.pitonite.exch_cx.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.mappers.toOrderEntity
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateEntity
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.api.OrderResponse
import io.github.pitonite.exch_cx.utils.ExchParser
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl
@Inject
constructor(
    private val exchDatabase: ExchDatabase,
    private val exchHttpClient: ExchHttpClient,
) : OrderRepository {

  override fun getOrderList(archived: Boolean): Flow<PagingData<Order>> {
    return Pager(
            config = PagingConfig(pageSize = 10),
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

  override suspend fun fetchAndUpdateOrder(orderid: String): Boolean {
    val orderUpdate = this.fetchOrder(orderid).toOrderUpdateEntity()
    return this.updateOrder(orderUpdate)
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
