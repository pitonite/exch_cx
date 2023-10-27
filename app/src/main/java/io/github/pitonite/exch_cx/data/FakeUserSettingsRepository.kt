package io.github.pitonite.exch_cx.data

import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class FakeUserSettingsRepository(
    private val userSettings: UserSettings = UserSettings.getDefaultInstance()
) : UserSettingsRepository {

  override val userSettingsFlow: Flow<UserSettings> = flow { emit(userSettings) }

  override suspend fun fetchSettings() = userSettingsFlow.first()

  override suspend fun updateApiKey(newKey: String) {}

  override suspend fun updateDomainOption(newDomainType: PreferredDomainType) {}

  override suspend fun saveSettings(userSettings: UserSettings) {}
}
