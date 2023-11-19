package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.model.api.SupportMessageSender
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Stable
class SupportMessagesRepositoryMock : SupportMessagesRepository {

  companion object {
    val messages =
        persistentListOf(
            SupportMessage(
                orderid = "ee902b8a5fe0844d41",
                message = "Hello",
                sender = SupportMessageSender.USER,
                readBySupport = true,
                index = 1,
            ),
            SupportMessage(
                orderid = "ee902b8a5fe0844d41",
                message = "Hi",
                sender = SupportMessageSender.SUPPORT,
                readBySupport = false,
                index = 2,
            ),
            SupportMessage(
                orderid = "ee902b8a5fe0844d41",
                message = "I have a problem",
                sender = SupportMessageSender.USER,
                readBySupport = true,
                index = 3,
            ),
            SupportMessage(
                orderid = "ee902b8a5fe0844d41",
                message = "Okay, can I ask what's the problem?",
                sender = SupportMessageSender.SUPPORT,
                readBySupport = false,
                index = 4,
            ),
            SupportMessage(
                orderid = "ee902b8a5fe0844d41",
                message = "The order is stuck at Exchanging state, can you help me with this?",
                sender = SupportMessageSender.USER,
                readBySupport = true,
                index = 5,
            ),
            SupportMessage(
                orderid = "ee902b8a5fe0844d41",
                message = "Sure",
                sender = SupportMessageSender.SUPPORT,
                readBySupport = false,
                index = 6,
            ),
        )
  }

  override fun getMessages(orderid: String, pageSize: Int): Flow<PagingData<SupportMessage>> {
    return flow {
      emit(
          PagingData.from(messages),
      )
    }
  }

  override suspend fun sendMessage(orderid: String, message: String) {}

  override suspend fun fetchAndUpdateMessages(orderid: String) {}

  override suspend fun fetchMessages(orderid: String): List<SupportMessage> {
    return messages
  }

  override suspend fun updateMessages(messages: List<SupportMessage>) {}

  override suspend fun updateMessage(message: SupportMessage) {}
  override suspend fun addUserMessage(orderid: String, message: String) {}
  override suspend fun countMessages(orderid: String): Int {
    return 0
  }
}
