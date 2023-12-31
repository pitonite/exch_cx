package io.github.pitonite.exch_cx.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.Date

/** [Room] DAO for [Message] related operations. */
@Dao
abstract class SupportMessageDao : BaseDao<Order> {

  @Transaction
  @Query("SELECT * FROM `SupportMessage` WHERE orderid = :orderid ORDER BY `index` DESC")
  abstract fun supportMessagesSortedByIndexPagingSource(
    orderid: String,
  ): PagingSource<Int, SupportMessage>

  @Query("SELECT COUNT(*) FROM `SupportMessage` where orderid = :orderid")
  abstract suspend fun count(orderid: String): Int

  @Upsert(entity = SupportMessage::class) abstract suspend fun upsert(entity: SupportMessage)

  @Upsert(entity = SupportMessage::class) abstract suspend fun upsertMessages(entities: List<SupportMessage>)

  @Query("INSERT OR IGNORE INTO `SupportMessage` (orderid, 'index', readBySupport, sender, message)  VALUES (:orderid, (SELECT `index` + 1 FROM `SupportMessage` where orderid = :orderid ORDER by `index` DESC LIMIT 1) , 0, \"USER\", :message)")
  abstract suspend fun addUserMessage(orderid: String, message: String)
}
