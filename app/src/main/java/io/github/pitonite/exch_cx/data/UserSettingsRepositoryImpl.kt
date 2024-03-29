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
import io.github.pitonite.exch_cx.PreferredProxyType
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.copy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

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

  override suspend fun saveSettings(userSettings: UserSettings) {
    val oldSettings = fetchSettings()
    exchWorkManager.adjustAutoUpdater(
        userSettings,
        if (userSettings.orderAutoUpdatePeriodMinutes != oldSettings.orderAutoUpdatePeriodMinutes)
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
        else ExistingPeriodicWorkPolicy.UPDATE,
    )
    exchWorkManager.adjustReserveCheckWorker(
        userSettings,
        ExistingPeriodicWorkPolicy.UPDATE,
        false,
    )
    userSettingsStore.updateData { it.toBuilder().clear().mergeFrom(userSettings).build() }
  }

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

  override suspend fun setDeleteRemoteOrderDataAutomatically(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setDeleteRemoteOrderDataAutomatically(value).build()
    }
  }

  override suspend fun setIsProxyEnabled(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setIsProxyEnabled(value).build()
    }
  }

  override suspend fun setProxyHost(value: String) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setProxyHost(value).build()
    }
  }

  override suspend fun setProxyPort(value: String) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setProxyPort(value).build()
    }
  }

  override suspend fun setPreferredProxyType(value: PreferredProxyType) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setPreferredProxyType(value).build()
    }
  }

  override suspend fun setIsReserveCheckEnabled(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setIsReserveCheckEnabled(value).build()
    }
  }

  override suspend fun setReserveCheckPeriodMinutes(value: Long) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setReserveCheckPeriodMinutes(value).build()
    }
  }

  override suspend fun setIsReserveCheckTipDismissed(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setIsReserveCheckTipDismissed(value).build()
    }
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
