package io.github.pitonite.exch_cx.data

import android.util.Log
import androidx.datastore.core.DataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.ExchDomainType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// does not need to be singleton since the underlying data store is singleton
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

  override suspend fun fetchSettings() = userSettingsStore.data.first()

  override suspend fun updateApiKey(newKey: String) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setApiKey(newKey).build()
    }
  }

  override suspend fun updateDomainOption(newDomainType: ExchDomainType) {
    userSettingsStore.updateData { currentSettings ->
      currentSettings.toBuilder().setDomainType(newDomainType).build()
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
