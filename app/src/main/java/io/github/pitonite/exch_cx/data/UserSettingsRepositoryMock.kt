package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.PreferredProxyType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@Stable
class UserSettingsRepositoryMock(
    private val userSettings: UserSettings = UserSettings.getDefaultInstance()
) : UserSettingsRepository {

  override val userSettingsFlow: Flow<UserSettings> = flow { emit(userSettings) }

  override suspend fun fetchSettings() = userSettingsFlow.first()

  override suspend fun saveSettings(userSettings: UserSettings) {}

  override suspend fun setApiKey(newKey: String) {}

  override suspend fun setDomainOption(newDomainType: PreferredDomainType) {}

  override suspend fun setExchangeTipDismissed(value: Boolean) {}

  override suspend fun setIsOrderAutoUpdateEnabled(value: Boolean) {}

  override suspend fun setHasShownOrderBackgroundUpdateNotice(value: Boolean) {}

  override suspend fun setArchiveOrdersAutomatically(value: Boolean) {}

  override suspend fun setFirstInitDone(value: Boolean) {}

  override suspend fun setOrderAutoUpdatePeriodMinutes(value: Long) {}

  override suspend fun setDeleteRemoteOrderDataAutomatically(value: Boolean) {}

  override suspend fun setIsProxyEnabled(value: Boolean) {}

  override suspend fun setProxyHost(value: String) {}

  override suspend fun setProxyPort(value: String) {}

  override suspend fun setPreferredProxyType(value: PreferredProxyType) {}

  override suspend fun setIsReserveCheckEnabled(value: Boolean) {}

  override suspend fun setReserveCheckPeriodMinutes(value: Long) {}

  override suspend fun setIsReserveCheckTipDismissed(value: Boolean) {}
}
