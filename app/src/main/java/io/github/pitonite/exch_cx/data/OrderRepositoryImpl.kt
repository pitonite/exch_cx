package io.github.pitonite.exch_cx.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateEntity
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.api.OrderResponse
import io.ktor.client.call.body
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

  override suspend fun updateOrder(orderId: String): Boolean {
    val resp =
        exchHttpClient
            .get("/api/order/") { url { parameters.append("orderid", orderId) } }
            .body<OrderResponse>()
    val existedBeforeUpsert = exchDatabase.ordersDao().exists(orderId)
    exchDatabase.ordersDao().upsert(resp.toOrderUpdateEntity())
    return existedBeforeUpsert
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
