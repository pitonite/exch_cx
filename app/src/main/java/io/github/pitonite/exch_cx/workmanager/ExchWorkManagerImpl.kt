package io.github.pitonite.exch_cx.workmanager

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.worker.BackupWorker
import io.github.pitonite.exch_cx.worker.DomainCheckWorker
import io.github.pitonite.exch_cx.worker.OrderAutoUpdateWorker
import io.github.pitonite.exch_cx.worker.ReserveCheckWorker
import io.github.pitonite.exch_cx.worker.RestoreWorker
import io.github.pitonite.exch_cx.worker.backupWorkName
import io.github.pitonite.exch_cx.worker.domainCheckWorkName
import io.github.pitonite.exch_cx.worker.orderAutoUpdateWorkName
import io.github.pitonite.exch_cx.worker.orderAutoUpdateWorkNameOneTime
import io.github.pitonite.exch_cx.worker.reserveCheckWorkName
import io.github.pitonite.exch_cx.worker.restoreWorkName
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val CurrentWorkProgress = "Progress"
const val TotalWorkItems = "TotalItems"

@Stable
@Singleton
class ExchWorkManagerImpl
@Inject
constructor(
    @ApplicationContext private val context: Context,
) : ExchWorkManager {

  override fun adjustAutoUpdater(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
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

  override fun stopAutoUpdate() {
    WorkManager.getInstance(context).cancelUniqueWork(orderAutoUpdateWorkName)
  }

  override fun startOneTimeOrderUpdate() {
    val workRequest = OneTimeWorkRequestBuilder<OrderAutoUpdateWorker>().build()
    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            orderAutoUpdateWorkNameOneTime,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
  }

  override fun stopOneTimeOrderUpdate() {
    WorkManager.getInstance(context).cancelUniqueWork(orderAutoUpdateWorkNameOneTime)
  }

  override fun getAutoUpdateWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(orderAutoUpdateWorkName)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  override fun getOneTimeOrderUpdateWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(orderAutoUpdateWorkNameOneTime)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  override fun getDomainCheckWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(domainCheckWorkName)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  override fun getReserveCheckWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(reserveCheckWorkName)
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }
  }

  override fun adjustReserveCheckWorker(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
      immediate: Boolean,
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

  override fun startDomainCheck() {
    val workRequest = OneTimeWorkRequestBuilder<DomainCheckWorker>().build()
    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            domainCheckWorkName,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
  }

  override fun stopDomainCheck() {
    WorkManager.getInstance(context).cancelUniqueWork(domainCheckWorkName)
  }

  override fun startBackup(fileUri: Uri): Operation  {
    val workRequest =
        OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(workDataOf("FILE_URI" to fileUri.toString()))
            .build()
    return WorkManager.getInstance(context)
        .enqueueUniqueWork(
            backupWorkName,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
  }
  override fun startRestore(fileUri: Uri): Operation {
    val workRequest =
        OneTimeWorkRequestBuilder<RestoreWorker>()
            .setInputData(workDataOf("FILE_URI" to fileUri.toString()))
            .build()
    return WorkManager.getInstance(context)
        .enqueueUniqueWork(
            restoreWorkName,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
  }

  override fun getBackupWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(backupWorkName)
        .map { workInfos -> workInfos.firstOrNull() }
  }

  override fun getRestoreWorkInfo(): Flow<WorkInfo?> {
    return WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(restoreWorkName)
        .map { workInfos -> workInfos.firstOrNull() }
  }
}
