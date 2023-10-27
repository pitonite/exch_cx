package io.github.pitonite.exch_cx.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** [Room] DAO for [Order] related operations. */
@Dao
abstract class OrderDao : BaseDao<Order> {
  @Query("SELECT * FROM `order` WHERE id = :id") abstract fun orderWithId(id: String): Flow<Order>

  @Transaction
  @Query("SELECT * FROM `Order` WHERE archived = :archived ORDER BY createdAt ASC")
  abstract fun ordersSortedByCreatedAtPagingSource(
      archived: Boolean = false,
  ): PagingSource<Int, Order>

  @Query("SELECT COUNT(*) FROM `Order`") abstract suspend fun count(): Int

  @Query("SELECT COUNT(*) FROM `Order` where archived = 1")
  abstract suspend fun countArchived(): Int

  @Query("SELECT COUNT(*) FROM `Order` where archived = 0") abstract suspend fun countActive(): Int

  @Upsert(entity = Order::class) abstract suspend fun upsert(entity: OrderUpdate)

  @Query("SELECT EXISTS(SELECT 1 FROM `order` WHERE id = :id LIMIT 1)")
  abstract suspend fun exists(id: String): Boolean
}
