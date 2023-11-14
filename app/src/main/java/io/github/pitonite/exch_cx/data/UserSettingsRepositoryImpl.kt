package io.github.pitonite.exch_cx.data

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStore
import androidx.work.ExistingPeriodicWorkPolicy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.copy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Stable
class UserSettingsRepositoryImpl
@Inject
constructor(
    private val userSettingsStore: DataStore<UserSettings>,
    private val exchWorkManager: ExchWorkManager,
) : UserSettingsRepository {

  companion object {
    const val TAG = "UserSettingsRepository"
  }

  override val userSettingsFlow: Flow<UserSettings> =
      userSettingsStore.data.catch { exception ->
        if (exception is IOException) {
          Log.e(TAG, "Error reading user settings.", exception)
          emit(UserSettings.getDefaultInstance())
        } else {
          throw exception
        }
      }

  override suspend fun fetchSettings() = userSettingsFlow.first()

  override suspend fun setApiKey(newKey: String) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setApiKey(newKey).build()
    }
  }

  override suspend fun setDomainOption(newDomainType: PreferredDomainType) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setPreferredDomainType(newDomainType).build()
    }
  }

  override suspend fun setExchangeTipDismissed(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setIsExchangeTipDismissed(value).build()
    }
  }

  override suspend fun setIsOrderAutoUpdateEnabled(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setIsOrderAutoUpdateEnabled(value).build()
    }
    exchWorkManager.adjustAutoUpdater(fetchSettings().copy { isOrderAutoUpdateEnabled = value })
  }

  override suspend fun setHasShownOrderBackgroundUpdateNotice(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setHasShownOrderBackgroundUpdateNotice(value).build()
    }
  }

  override suspend fun setArchiveOrdersAutomatically(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setArchiveOrdersAutomatically(value).build()
    }
  }

  override suspend fun setFirstInitDone(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setFirstInitDone(value).build()
    }
  }

  override suspend fun setOrderAutoUpdatePeriodMinutes(value: Long) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setOrderAutoUpdatePeriodMinutes(value).build()
    }
    exchWorkManager.adjustAutoUpdater(
        fetchSettings().copy { orderAutoUpdatePeriodMinutes = value },
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
  }

  override suspend fun saveSettings(userSettings: UserSettings) {
    exchWorkManager.adjustAutoUpdater(
        userSettings,
        if (userSettings.orderAutoUpdatePeriodMinutes !=
            fetchSettings().orderAutoUpdatePeriodMinutes)
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
        else ExistingPeriodicWorkPolicy.UPDATE)
    userSettingsStore.updateData { it.toBuilder().clear().mergeFrom(userSettings).build() }
  }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSettingsRepositoryModule {
  @Binds
  @Singleton
  abstract fun provideUserSettingsRepository(
      userSettingsRepository: UserSettingsRepositoryImpl
  ): UserSettingsRepository
}
