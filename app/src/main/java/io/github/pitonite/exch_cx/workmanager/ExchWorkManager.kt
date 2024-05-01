package io.github.pitonite.exch_cx.workmanager

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.worker.OrderAutoUpdateWorker
import io.github.pitonite.exch_cx.worker.orderAutoUpdateWorkName
import kotlinx.coroutines.flow.Flow

@Stable
interface ExchWorkManager {

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

  fun adjustAutoUpdater(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
  )

  fun stopAutoUpdate()

  fun startOneTimeOrderUpdate()

  fun stopOneTimeOrderUpdate()

  fun getAutoUpdateWorkInfo(): Flow<WorkInfo?>

  fun getOneTimeOrderUpdateWorkInfo(): Flow<WorkInfo?>

  fun getDomainCheckWorkInfo(): Flow<WorkInfo?>

  fun getReserveCheckWorkInfo(): Flow<WorkInfo?>

  fun adjustReserveCheckWorker(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
      immediate: Boolean = true
  )

  fun startDomainCheck()
  fun stopDomainCheck()
}
