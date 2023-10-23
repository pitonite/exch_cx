package io.github.pitonite.exch_cx.data

import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.userSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class FakeUserSettingsRepository : UserSettingsRepository {
  companion object {
    val mockSettings = userSettings {
      apiKey = "foobarapikey"
      preferredDomainType = PreferredDomainType.ONION
    }
  }

  override val userSettingsFlow: Flow<UserSettings> = flow { emit(mockSettings) }

  override suspend fun fetchSettings() = userSettingsFlow.first()

  override suspend fun updateApiKey(newKey: String) {}

  override suspend fun updateDomainOption(newDomainType: PreferredDomainType) {}

  override suspend fun saveSettings(userSettings: UserSettings) {}
}
