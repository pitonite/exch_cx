package io.github.pitonite.exch_cx.workmanager

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ExchWorkManagerMock : ExchWorkManager {
  override fun adjustAutoUpdater(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy
  ) {}

  override fun stopAutoUpdate() {}

  override fun startOneTimeOrderUpdate() {}

  override fun stopOneTimeOrderUpdate() {}

  override fun getAutoUpdateWorkInfo(): Flow<WorkInfo?> {
    return flow { emit(null) }
  }

  override fun getOneTimeOrderUpdateWorkInfo(): Flow<WorkInfo?> {
    return flow { emit(null) }
  }

  override fun getDomainCheckWorkInfo(): Flow<WorkInfo?> {
    return flow { emit(null) }
  }

  override fun getReserveCheckWorkInfo(): Flow<WorkInfo?> {
    return flow { emit(null) }
  }

  override fun adjustReserveCheckWorker(
      userSettings: UserSettings,
      existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
      immediate: Boolean
  ) {}

  override fun startDomainCheck() {}

  override fun stopDomainCheck() {}
}
