package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.PreferredProxyType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow

@Stable
interface UserSettingsRepository {
  /** exposed flow to the user settings datastore */
  val userSettingsFlow: Flow<UserSettings>

  suspend fun fetchSettings(): UserSettings

  suspend fun saveSettings(userSettings: UserSettings)

  suspend fun setApiKey(newKey: String)

  /** sets the preferred domain for accessing exch.cx website */
  suspend fun setDomainOption(newDomainType: PreferredDomainType)

  suspend fun setExchangeTipDismissed(value: Boolean)

  suspend fun setIsOrderAutoUpdateEnabled(value: Boolean)

  suspend fun setHasShownOrderBackgroundUpdateNotice(value: Boolean)

  suspend fun setArchiveOrdersAutomatically(value: Boolean)

  suspend fun setFirstInitDone(value: Boolean)

  suspend fun setOrderAutoUpdatePeriodMinutes(value: Long)

  suspend fun setDeleteRemoteOrderDataAutomatically(value: Boolean)

  suspend fun setIsProxyEnabled(value: Boolean)
  suspend fun setProxyHost(value: String)
  suspend fun setProxyPort(value: String)
  suspend fun setPreferredProxyType(value: PreferredProxyType)
}
