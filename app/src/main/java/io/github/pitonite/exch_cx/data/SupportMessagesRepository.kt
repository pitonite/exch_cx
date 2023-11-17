package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import io.github.pitonite.exch_cx.data.room.SupportMessage
import kotlinx.coroutines.flow.Flow

@Stable
interface SupportMessagesRepository {

  fun getMessages(orderid: String, pageSize: Int = 10): Flow<PagingData<SupportMessage>>

  suspend fun sendMessage(orderid: String, message: String)

  suspend fun fetchAndUpdateMessages(orderid: String)

  suspend fun fetchMessages(orderid: String): List<SupportMessage>

  suspend fun updateMessages(messages: List<SupportMessage>)
  suspend fun updateMessage(message: SupportMessage)

  /** this is for inserting a user message locally, for when sending message is successful, without the need to refresh */
  suspend fun addUserMessage(orderid: String, message: String)
}
