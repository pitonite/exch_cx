package io.github.pitonite.exch_cx.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.Date

/** [Room] DAO for [Order] related operations. */
@Dao
abstract class OrderDao : BaseDao<Order> {
  @Query("SELECT * FROM `order` WHERE id = :id") abstract fun orderWithId(id: String): Flow<Order?>

  @Query(
      "SELECT * FROM `order` WHERE archived = :archived AND createdAt > :createdAt ORDER BY createdAt ASC LIMIT 1")
  abstract fun orderAfter(createdAt: Date, archived: Boolean): Order?

  @Transaction
  @Query("SELECT * FROM `Order` WHERE archived = :archived ORDER BY createdAt DESC")
  abstract fun ordersSortedByCreatedAtPagingSource(
      archived: Boolean = false,
  ): PagingSource<Int, Order>

  @Query("SELECT COUNT(*) FROM `Order` where archived = :archived")
  abstract suspend fun count(archived: Boolean): Int

  @Upsert(entity = Order::class) abstract suspend fun upsert(entity: OrderUpdate)

  @Upsert(entity = Order::class) abstract suspend fun upsert(entity: OrderUpdateWithArchive)

  @Upsert(entity = Order::class) abstract suspend fun upsert(entity: OrderCreate)

  @Query("SELECT EXISTS(SELECT 1 FROM `order` WHERE id = :id LIMIT 1)")
  abstract suspend fun exists(id: String): Boolean

  @Update(entity = Order::class) abstract suspend fun setArchive(entity: OrderArchive)
  @Query("DELETE FROM `order` WHERE id = :orderid")
  abstract suspend fun delete(orderid: String)
}
