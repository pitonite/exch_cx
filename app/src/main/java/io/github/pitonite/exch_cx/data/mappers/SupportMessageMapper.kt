package io.github.pitonite.exch_cx.data.mappers

import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.model.api.SupportMessagesResponse
import java.util.Date

fun SupportMessagesResponse.toSupportMessages(orderid: String): List<SupportMessage> {
  return this.messages.mapIndexed { index, resp ->
    SupportMessage(
        orderid = orderid,
        index = index,
        message = resp.message,
        readBySupport = resp.readBySupport,
        createdAt = Date((resp.timestamp?.times(1000)) ?: System.currentTimeMillis()),
        sender = resp.sender,
    )
  }
}
