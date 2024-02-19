package io.github.pitonite.exch_cx

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pitonite.exch_cx.worker.OrderAutoUpdateWorker
import io.github.pitonite.exch_cx.worker.ReserveCheckWorker
import io.github.pitonite.exch_cx.worker.orderAutoUpdateWorkName
import io.github.pitonite.exch_cx.worker.orderAutoUpdateWorkNameOneTime
import io.github.pitonite.exch_cx.worker.reserveCheckWorkName
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val CurrentWorkProgress = "Progress"
const val TotalWorkItems = "TotalItems"

@Stable
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
          PeriodicWorkRequestBuilder<OrderAutoUpdateWorker>(updatePeriod, TimeUnit.MINUTES)
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

  fun startOneTimeOrderUpdate() {
    val workRequest = OneTimeWorkRequestBuilder<OrderAutoUpdateWorker>().build()
    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            orderAutoUpdateWorkNameOneTime,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
  }

  fun stopOneTimeOrderUpdate() {
    WorkManager.getInstance(context).cancelUniqueWork(orderAutoUpdateWorkNameOneTime)
  }

  fun getAutoUpdateWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(orderAutoUpdateWorkName)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  fun getOneTimeOrderUpdateWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(orderAutoUpdateWorkNameOneTime)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  fun getReserveCheckWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(reserveCheckWorkName)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  fun adjustReserveCheckWorker(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
      immediate: Boolean = true,
  ) {
    if (userSettings.isReserveCheckEnabled) {
      val updatePeriod =
          if (userSettings.reserveCheckPeriodMinutes <= 15) 15
          else userSettings.reserveCheckPeriodMinutes

      val workRequest =
          PeriodicWorkRequestBuilder<ReserveCheckWorker>(updatePeriod, TimeUnit.MINUTES)
              .setConstraints(
                  Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())

      if (!immediate) {
        workRequest.setInitialDelay(updatePeriod, TimeUnit.MINUTES)
      }

      WorkManager.getInstance(context)
          .enqueueUniquePeriodicWork(
              reserveCheckWorkName,
              existingPeriodicWorkPolicy,
              workRequest.build(),
          )
    } else {
      WorkManager.getInstance(context).cancelUniqueWork(reserveCheckWorkName)
    }
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

    fun cancelUniqueWork(context: Context, workName: String) {
      WorkManager.getInstance(context).cancelUniqueWork(workName)
    }
  }
}
