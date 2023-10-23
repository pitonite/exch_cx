package io.github.pitonite.exch_cx.data

import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
  /** exposed flow to the user settings datastore */
  val userSettingsFlow: Flow<UserSettings>

  suspend fun fetchSettings(): UserSettings

  suspend fun updateApiKey(newKey: String)

  /** sets the preferred domain for accessing exch.cx website */
  suspend fun updateDomainOption(newDomainType: PreferredDomainType)

  suspend fun saveSettings(userSettings: UserSettings)
}
