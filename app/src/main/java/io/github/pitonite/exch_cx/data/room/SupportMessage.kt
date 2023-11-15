package io.github.pitonite.exch_cx.data.room

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.github.pitonite.exch_cx.model.api.SupportMessageSender
import java.util.Date

@Entity(
    primaryKeys = ["orderid", "index"],
    indices = [Index("orderid", "index")],
    foreignKeys =
        [
            ForeignKey(
                entity = Order::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("orderid"),
                onDelete = ForeignKey.CASCADE)],
)
@Stable
data class SupportMessage(
    @ColumnInfo(index = true) val orderid: String,
    val index: Int,
    @ColumnInfo(defaultValue = CURRENT_TIMESTAMP_EXPRESSION) val createdAt: Date = Date(),
    @ColumnInfo(defaultValue = "0") val readBySupport: Boolean = false,
    val sender: SupportMessageSender,
    val message: String,
)
