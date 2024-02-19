package io.github.pitonite.exch_cx.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.MainActivity
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.CurrencyReserveRepository
import io.github.pitonite.exch_cx.data.CurrencyReserveTriggerRepository
import io.github.pitonite.exch_cx.utils.createNotificationChannels
import io.github.pitonite.exch_cx.utils.isNotificationAllowed
import java.util.Date

const val reserveCheckWorkName = "check_reserves_work"

@HiltWorker
class ReserveCheckWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val currencyReserveRepository: CurrencyReserveRepository,
    private val currencyReserveTriggerRepository: CurrencyReserveTriggerRepository,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG: String = "ReserveCheckWorker"
  }

  @SuppressLint("MissingPermission")
  override suspend fun doWork(): Result {
    Log.i(TAG, "Starting ReserveCheckWorker at ${Date()}")

    val context = this.applicationContext

    val notificationAllowed = isNotificationAllowed(context)
    if (!notificationAllowed) {
      Log.e(
          TAG,
          "sending notifications is not allowed, it doesn't make sense to check for reserves. cancelling the job.")
      ExchWorkManager.cancelUniqueWork(context, reserveCheckWorkName)
      return Result.failure()
    }
    val notifManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    createNotificationChannels(context)

    val triggers = currencyReserveTriggerRepository.getActiveTriggers()
    if (triggers.isEmpty()) {
      Log.e(
          TAG,
          "No active currency reserve trigger. Ending job early.")
      return Result.success()
    }
    val currentReserves =
        currencyReserveRepository.getCurrencyReserves().associateBy { it.currency }
    val newReserves = currencyReserveRepository.fetchAndUpdateReserves().associateBy { it.currency }

    newReserves.keys.forEach {
      val oldAmount = currentReserves[it]?.amount
      val newAmount = newReserves[it]!!.amount

      val filteredTriggers = triggers.filter { t -> t.currency == it }
      if (filteredTriggers.isNotEmpty()) {
        for (trigger in filteredTriggers) {
          var shouldNotify = false
          if (trigger.comparison == null || trigger.targetAmount == null) {
            if(oldAmount == null || newAmount.compareTo(oldAmount) != 0) {
              shouldNotify = true
            }
          } else if (newAmount.compareTo(trigger.targetAmount) == trigger.comparison) {
            // these two (up and below) ifs meaning:
            // when the condition has matched, and old condition was not matching.
            if (oldAmount == null ||
                oldAmount.compareTo(trigger.targetAmount) != trigger.comparison) {
              shouldNotify = true
            }
          }

          if (shouldNotify) {
            val notifTag = "reserve_trigger:${trigger.id}"

            val notifBuilder =
                NotificationCompat.Builder(
                        context, context.getString(R.string.channel_id_reserve_alert))
                    .setSmallIcon(R.drawable.x_large)
                    .setContentTitle(
                        context.getString(
                            R.string.currency_reserve_alert,
                        ),
                    )
                    .setContentText(
                        context.getString(
                            R.string.currency_reserve_amount_is_now_at,
                            trigger.currency,
                            newAmount.stripTrailingZeros().toString(),
                        ),
                    )
                    .setContentIntent(
                        PendingIntentCompat.getActivity(
                            context,
                            0,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT,
                            false,
                        ),
                    )
                    .setAutoCancel(true)

            notifManager.notify(notifTag, R.id.notif_id_reserve_alert, notifBuilder.build())

            if (trigger.onlyOnce) {
              try {
                currencyReserveTriggerRepository.updateTrigger(
                    trigger.copy(
                        isEnabled = false,
                    ),
                )
              } catch (e: Throwable) {
                Log.i(TAG, "it seems user had deleted trigger (?!), skipping updating the trigger.")
              }
            }
          }
        }
      }
    }

    return Result.success()
  }
}
