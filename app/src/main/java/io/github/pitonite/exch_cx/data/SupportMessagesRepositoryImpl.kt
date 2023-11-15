package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.data.mappers.toSupportMessages
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.api.BooleanResult
import io.github.pitonite.exch_cx.model.api.SupportMessagesResponse
import io.github.pitonite.exch_cx.model.api.exceptions.FailedToSendSupportMessageException
import io.ktor.client.call.body
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
@Stable
class SupportMessagesRepositoryImpl
@Inject
constructor(
    private val exchHttpClient: ExchHttpClient,
    private val exchDatabase: ExchDatabase,
) : SupportMessagesRepository {
  override fun getMessages(orderid: String, pageSize: Int): Flow<PagingData<SupportMessage>> {
    return Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = {
              exchDatabase.supportMessagesDao().supportMessagesSortedByIndexPagingSource(orderid)
            },
        )
        .flow
  }

  override suspend fun sendMessage(orderid: String, message: String) {
    val resp: BooleanResult =
        exchHttpClient
            .get("/api/order/support_message") {
              url {
                parameters.append("orderid", orderid)
                parameters.append("supportmessage", message)
              }
            }
            .body()
    if (!resp.result) throw FailedToSendSupportMessageException()
  }

  override suspend fun fetchAndUpdateMessages(orderid: String) {
    updateMessages(fetchMessages(orderid))
  }

  override suspend fun fetchMessages(orderid: String): List<SupportMessage> {
    val resp: SupportMessagesResponse = exchHttpClient.get("/api/order/support_messages") {
      url {
        parameters.append("orderid", orderid)
      }
    }.body()

    return resp.toSupportMessages(orderid)
  }

  override suspend fun updateMessage(message: SupportMessage) {
    exchDatabase.supportMessagesDao().upsert(message)
  }

  override suspend fun updateMessages(messages: List<SupportMessage>) {
    exchDatabase.supportMessagesDao().upsertMessages(messages)
  }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SupportMessagesRepositoryModule {
  @Binds
  @Singleton
  abstract fun provideSupportMessagesRepositoryModule(
      supportMessagesRepositoryImpl: SupportMessagesRepositoryImpl
  ): SupportMessagesRepository
}
