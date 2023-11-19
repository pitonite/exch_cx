package io.github.pitonite.exch_cx.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.pitonite.exch_cx.CurrentWorkProgress
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.TotalWorkItems
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.SupportMessagesRepository
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.data.mappers.toOrderUpdateWithArchiveEntity
import io.github.pitonite.exch_cx.getOrderDeepLinkPendingIntent
import io.github.pitonite.exch_cx.getOrderSupportDeepLinkPendingIntent
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.SupportMessageSender
import io.github.pitonite.exch_cx.utils.codified.enums.toLocalizedString
import io.github.pitonite.exch_cx.utils.createNotificationChannels
import io.github.pitonite.exch_cx.utils.isNotificationAllowed
import java.util.Date

const val orderAutoUpdateWorkName = "auto_update_orders"
const val orderAutoUpdateWorkNameOneTime = "auto_update_orders_once"

@HiltWorker
class OrderAutoUpdateWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val orderRepository: OrderRepository,
    private val supportMessagesRepository: SupportMessagesRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val exchWorkManager: ExchWorkManager,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG: String = "OrderUpdaterWorker"
  }

  @SuppressLint("MissingPermission")
  override suspend fun doWork(): Result {
    Log.e(TAG, "Starting OrderUpdaterWorker at ${Date()}")

    val settings = userSettingsRepository.fetchSettings()
    val context = this.applicationContext

    val notificationAllowed = isNotificationAllowed(context)
    val notifManager: NotificationManagerCompat?
    if (notificationAllowed) {
      notifManager = NotificationManagerCompat.from(context)
      createNotificationChannels(context) // just in case where channel was not created
    } else {
      notifManager = null
    }

    val totalCount = orderRepository.count(false)
    var progress = 0
    if (totalCount == 0) {
      exchWorkManager.stopAutoUpdate()
    } else {
      var dateCondition = Date(0)
      do {
        val order = orderRepository.getOrderAfter(dateCondition, false)
        setProgress(workDataOf(TotalWorkItems to totalCount, CurrentWorkProgress to ++progress))
        if (order != null) {
          dateCondition = order.createdAt
          Log.e(TAG, "Trying to update order #${order.id}")

          val hasLetterOfGuarantee = !order.letterOfGuarantee.isNullOrEmpty()
          val hasLetterOfGuaranteeConditions =
              order.stateError == null && order.state.knownOrNull() != OrderState.CREATED
          try {
            val fetchedOrder = orderRepository.fetchOrder(order.id)

            val archived =
                if (settings.archiveOrdersAutomatically) {
                  when (fetchedOrder.state.knownOrNull()) {
                    // terminal states here:
                    OrderState.COMPLETE,
                    OrderState.REFUNDED,
                    OrderState.CANCELLED -> true
                    else -> false
                  }
                } else false

            val orderUpdate = fetchedOrder.toOrderUpdateWithArchiveEntity(archived = archived)
            orderRepository.updateOrder(orderUpdate)

            // fetch letter of guarantee
            if (!hasLetterOfGuarantee && hasLetterOfGuaranteeConditions) {
              try {
                orderRepository.fetchAndUpdateLetterOfGuarantee(order.id)
              } catch (e: Exception) {
                // no need
              }
            }

            // fetch support messages if needed
            val messagesCount = supportMessagesRepository.countMessages(order.id)
            if (messagesCount > 0) {
              try {
                val messages = supportMessagesRepository.fetchMessages(order.id)
                if (messages.size > messagesCount &&
                    messages.lastOrNull()?.let { it.sender == SupportMessageSender.SUPPORT } ==
                        true) {
                  supportMessagesRepository.updateMessages(messages)
                  // tell user about new message from support
                  if (notifManager != null) {
                    val notifTag = "order:${order.id}"

                    val notifBuilder =
                        NotificationCompat.Builder(
                                context, context.getString(R.string.channel_id_order_support))
                            .setSmallIcon(R.drawable.x_large)
                            .setContentTitle(context.getString(R.string.order) + " " + order.id)
                            .setContentText(
                                context.getString(R.string.you_have_a_new_message_from_support))
                            .setContentIntent(
                                getOrderSupportDeepLinkPendingIntent(context, order.id))
                            .setAutoCancel(true)

                    notifManager.notify(
                        notifTag, R.id.notif_id_order_support_new_message, notifBuilder.build())
                  }
                }
              } catch (e: Exception) {
                // no need
              }
            }

            val stateHasChanged = order.state != orderUpdate.state
            val stateErrorHasChanged =
                order.stateError != orderUpdate.stateError && orderUpdate.stateError != null

            if (notifManager != null && (stateHasChanged || stateErrorHasChanged)) {
              val notifTag = "order:${order.id}"
              val stateTranslation =
                  if (stateHasChanged) orderUpdate.state.toLocalizedString(context)
                  else orderUpdate.stateError!!.toLocalizedString(context)

              val notifBuilder =
                  NotificationCompat.Builder(
                          context, context.getString(R.string.channel_id_order_state_change))
                      .setSmallIcon(R.drawable.x_large)
                      .setContentTitle(context.getString(R.string.order) + " " + order.id)
                      .setContentText(
                          context.getString(R.string.order_state_change, stateTranslation))
                      .setContentIntent(getOrderDeepLinkPendingIntent(context, order.id))
                      .setAutoCancel(true)

              notifManager.notify(notifTag, R.id.notif_id_order_state_change, notifBuilder.build())
            }

            if (stateHasChanged && orderUpdate.state.knownOrNull() == OrderState.COMPLETE && settings.deleteRemoteOrderDataAutomatically) {
              try {
                  orderRepository.deleteRemote(order.id)
              } catch (e: Exception) {
                if (notifManager != null) {
                  val notifTag = "order:${order.id}"

                  val notifBuilder =
                      NotificationCompat.Builder(
                          context, context.getString(R.string.channel_id_order_deletion))
                          .setSmallIcon(R.drawable.x_large)
                          .setContentTitle(context.getString(R.string.order) + " " + order.id)
                          .setContentText(
                              context.getString(R.string.failed_to_delete_remote_order_data))
                          .setContentIntent(getOrderDeepLinkPendingIntent(context, order.id))
                          .setAutoCancel(true)

                  notifManager.notify(notifTag, R.id.notif_id_order_delete_failed, notifBuilder.build())
                }
              }
            }
          } catch (e: Exception) {
            Log.e(TAG, e.message ?: e.toString())
            if (e.message?.contains("not found") == true && settings.archiveOrdersAutomatically) {
              // order needs to be archived
              orderRepository.updateOrder(order.copy(archived = true))
            }
          }
        }
      } while (order != null)
    }

    return Result.success()
  }
}
