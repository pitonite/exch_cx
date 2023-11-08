package io.github.pitonite.exch_cx

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pitonite.exch_cx.worker.OrderAutoUpdateWorker
import io.github.pitonite.exch_cx.worker.orderAutoUpdateWorkName
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchWorkManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
  fun adjustAutoUpdater(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
  ) {
    if (userSettings.isOrderAutoUpdateEnabled) {
      val updatePeriod =
          if (userSettings.orderAutoUpdatePeriodMinutes <= 15) 15
          else userSettings.orderAutoUpdatePeriodMinutes

      val workRequest =
          PeriodicWorkRequestBuilder<OrderAutoUpdateWorker>(
                  updatePeriod, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
              .setConstraints(
                  Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
              .build()
      WorkManager.getInstance(context)
          .enqueueUniquePeriodicWork(
              orderAutoUpdateWorkName,
              existingPeriodicWorkPolicy,
              workRequest,
          )
    } else {
      WorkManager.getInstance(context).cancelUniqueWork(orderAutoUpdateWorkName)
    }
  }

  fun stopAutoUpdate() {
    WorkManager.getInstance(context).cancelUniqueWork(orderAutoUpdateWorkName)
  }

  companion object {
    fun runAutoUpdaterOnce(context: Context) {
      WorkManager.getInstance(context)
          .enqueueUniqueWork(
              orderAutoUpdateWorkName,
              ExistingWorkPolicy.REPLACE,
              OneTimeWorkRequestBuilder<OrderAutoUpdateWorker>()
                  .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                  .build(),
          )
    }
  }
}


