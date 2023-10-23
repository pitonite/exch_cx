package io.github.pitonite.exch_cx.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.datastore.UserSettingsSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val SETTINGS_DATASTORE_FILE_NAME = "user_settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

  @Singleton
  @Provides
  fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<UserSettings> {
    return DataStoreFactory.create(
        serializer = UserSettingsSerializer,
        produceFile = { appContext.dataStoreFile(SETTINGS_DATASTORE_FILE_NAME) },
        corruptionHandler =
            ReplaceFileCorruptionHandler(produceNewData = { UserSettings.getDefaultInstance() }),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    )
  }
}
