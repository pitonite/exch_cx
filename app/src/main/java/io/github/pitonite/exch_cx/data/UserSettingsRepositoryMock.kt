package io.github.pitonite.exch_cx.data

import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class UserSettingsRepositoryMock(
    private val userSettings: UserSettings = UserSettings.getDefaultInstance()
) : UserSettingsRepository {

  override val userSettingsFlow: Flow<UserSettings> = flow { emit(userSettings) }

  override suspend fun fetchSettings() = userSettingsFlow.first()

  override suspend fun setApiKey(newKey: String) {}

  override suspend fun setDomainOption(newDomainType: PreferredDomainType) {}

  override suspend fun setExchangeTipDismissed(value: Boolean) {}

  override suspend fun setIsOrderAutoUpdateEnabled(value: Boolean) {}

  override suspend fun setHasShownOrderBackgroundUpdateNotice(value: Boolean) {}

  override suspend fun setArchiveOrdersAutomatically(value: Boolean) {}

  override suspend fun setFirstInitDone(value: Boolean) {}

  override suspend fun setOrderAutoUpdatePeriodMinutes(value: Long) {}

  override suspend fun saveSettings(userSettings: UserSettings) {}
}
