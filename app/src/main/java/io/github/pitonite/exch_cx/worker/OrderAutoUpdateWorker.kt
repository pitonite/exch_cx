package io.github.pitonite.exch_cx.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateEntity
import io.github.pitonite.exch_cx.utils.createNotificationChannels
import io.github.pitonite.exch_cx.utils.isNotificationAllowed
import java.util.Date

const val orderAutoUpdateWorkName = "auto_update_orders"

@HiltWorker
class OrderAutoUpdateWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val orderRepository: OrderRepository,
    private val exchWorkManager: ExchWorkManager,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG: String = "OrderUpdaterWorker"
  }

  @SuppressLint("MissingPermission")
  override suspend fun doWork(): Result {
    val context = this.applicationContext

    val notificationAllowed = isNotificationAllowed(context)
    val notifManager: NotificationManagerCompat?
    if (notificationAllowed) {
      notifManager = NotificationManagerCompat.from(context)
      createNotificationChannels(context) // just in case where channel was not created
    } else {
      notifManager = null
    }

    if (orderRepository.count(false) == 0) {
      exchWorkManager.stopAutoUpdate()
    } else {
      var dateCondition = Date(0)

      do {
        val order = orderRepository.getOrderAfter(dateCondition, false)
        if (order != null) {
          dateCondition = order.createdAt
          try {
            val orderUpdate = orderRepository.fetchOrder(order.id).toOrderUpdateEntity()
            orderRepository.updateOrder(orderUpdate)

            if (notifManager != null && order.state != orderUpdate.state) {
              val notifTag = "order:${order.id}"
              val notifBuilder =
                  NotificationCompat.Builder(
                          context, context.getString(R.string.channel_id_order_state_change))
                      .setSmallIcon(R.drawable.ic_launcher_foreground)
                      .setContentTitle(context.getString(R.string.order) + " " + order.id)
                      .setContentText(
                          context.getString(
                              R.string.order_state_change, orderUpdate.state.toReadableString()))

              notifManager.notify(notifTag, R.id.notif_id_order_state_change, notifBuilder.build())
            }
          } catch (e: Exception) {
            Log.e(TAG, e.message ?: e.toString())
          }
        }
      } while (order != null)
    }

    return Result.success()
  }
}
