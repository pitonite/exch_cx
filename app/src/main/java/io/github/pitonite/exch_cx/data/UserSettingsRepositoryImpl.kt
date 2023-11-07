package io.github.pitonite.exch_cx.data

import android.util.Log
import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl
@Inject
constructor(private val userSettingsStore: DataStore<UserSettings>) : UserSettingsRepository {

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

  override suspend fun updateApiKey(newKey: String) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setApiKey(newKey).build()
    }
  }

  override suspend fun updateDomainOption(newDomainType: PreferredDomainType) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setPreferredDomainType(newDomainType).build()
    }
  }

  override suspend fun updateExchangeTipDismissed(value: Boolean) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setIsExchangeTipDismissed(value).build()
    }
  }

  override suspend fun saveSettings(userSettings: UserSettings) {
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
