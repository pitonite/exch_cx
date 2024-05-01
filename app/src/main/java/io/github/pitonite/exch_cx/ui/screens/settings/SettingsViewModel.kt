package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.workmanager.ExchWorkManager
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.PreferredProxyType
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.copy
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.exceptions.LocalizedException
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.utils.WorkState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@Stable
class SettingsViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userSettingsRepository: UserSettingsRepository,
    private val workManager: ExchWorkManager
) : ViewModel() {

  var apiKeyDraft by mutableStateOf("")
    private set

  var preferredDomainTypeDraft by mutableStateOf(PreferredDomainType.NORMAL)
    private set

  var isOrderAutoUpdateEnabledDraft by mutableStateOf(false)
    private set

  var orderAutoUpdatePeriodMinutesDraft by mutableLongStateOf(0)
    private set

  var archiveOrdersAutomaticallyDraft by mutableStateOf(true)
    private set

  var deleteRemoteOrderDataAutomaticallyDraft by mutableStateOf(false)
    private set

  var isReserveCheckEnabledDraft by mutableStateOf(false)
    private set

  var reserveCheckPeriodMinutesDraft by mutableLongStateOf(0)
    private set

  var isProxyEnabledDraft by mutableStateOf(false)
    private set

  var proxyHostDraft by mutableStateOf("")
    private set

  var proxyPortDraft by mutableStateOf("")
    private set

  var preferredProxyTypeDraft by mutableStateOf(PreferredProxyType.SOCKS5)
    private set

  var selectedClearnetDomainIndexDraft by mutableIntStateOf(0)
    private set

  private val domainCheckWorkInfo =
      workManager
          .getDomainCheckWorkInfo()
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)

  val domainCheckWorkState =
      domainCheckWorkInfo
          .map { info ->
            when (info?.state) {
                WorkInfo.State.RUNNING -> {
                  WorkState.Working()
                }
                WorkInfo.State.FAILED -> {
                  WorkState.Error(LocalizedException(R.string.domain_check_failed))
                }
                else -> {
                  WorkState.NotWorking
                }
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = WorkState.NotWorking,
          )

  fun updateApiKeyDraft(value: String) {
    apiKeyDraft = value
  }

  fun updatePreferredDomainDraft(value: PreferredDomainType) {
    preferredDomainTypeDraft = value
  }

  fun updateIsOrderAutoUpdateEnabledDraft(value: Boolean) {
    isOrderAutoUpdateEnabledDraft = value
  }

  fun updateOrderAutoUpdatePeriodMinutesDraft(value: Long) {
    orderAutoUpdatePeriodMinutesDraft = value
  }

  fun updateArchiveOrdersAutomaticallyDraft(value: Boolean) {
    archiveOrdersAutomaticallyDraft = value
  }

  fun updateDeleteRemoteOrderDataAutomaticallyDraft(value: Boolean) {
    deleteRemoteOrderDataAutomaticallyDraft = value
  }

  fun updateIsReserveCheckEnabledDraft(value: Boolean) {
    isReserveCheckEnabledDraft = value
  }

  fun updateReserveCheckPeriodMinutesDraft(value: Long) {
    reserveCheckPeriodMinutesDraft = value
  }

  fun updateIsProxyEnabledDraft(value: Boolean) {
    isProxyEnabledDraft = value
  }

  fun updateProxyHostDraft(value: String) {
    proxyHostDraft = value
  }

  fun updateProxyPortDraft(value: String) {
    proxyPortDraft = value
  }

  fun updatePreferredProxyTypeDraft(value: PreferredProxyType) {
    preferredProxyTypeDraft = value
  }

  fun updateSelectedClearnetDomainIndexDraft(value: Int) {
    selectedClearnetDomainIndexDraft = value
  }

  fun reloadSettings() {
    viewModelScope.launch {
      userSettingsRepository.userSettingsFlow.firstOrNull()?.let {
        apiKeyDraft = it.apiKey
        preferredDomainTypeDraft = it.preferredDomainType
        isOrderAutoUpdateEnabledDraft = it.isOrderAutoUpdateEnabled
        orderAutoUpdatePeriodMinutesDraft = it.orderAutoUpdatePeriodMinutes
        archiveOrdersAutomaticallyDraft = it.archiveOrdersAutomatically
        deleteRemoteOrderDataAutomaticallyDraft = it.deleteRemoteOrderDataAutomatically
        isReserveCheckEnabledDraft = it.isReserveCheckEnabled
        reserveCheckPeriodMinutesDraft = it.reserveCheckPeriodMinutes
        isProxyEnabledDraft = it.isProxyEnabled
        proxyHostDraft = it.proxyHost
        proxyPortDraft = it.proxyPort
        preferredProxyTypeDraft = it.preferredProxyType
        selectedClearnetDomainIndexDraft = it.selectedClearnetDomainIndex
      }
    }
  }

  fun saveRequestSettings() {
    viewModelScope.launch {
      userSettingsRepository.saveSettings(
          userSettingsRepository.fetchSettings().copy {
            apiKey = apiKeyDraft
            preferredDomainType = preferredDomainTypeDraft
            selectedClearnetDomainIndex = selectedClearnetDomainIndexDraft
          })
      showSuccessSnack()
    }
  }

  fun saveAutoUpdateSettings() {
    viewModelScope.launch {
      userSettingsRepository.saveSettings(
          userSettingsRepository.fetchSettings().copy {
            isOrderAutoUpdateEnabled = isOrderAutoUpdateEnabledDraft
            orderAutoUpdatePeriodMinutes = orderAutoUpdatePeriodMinutesDraft
            archiveOrdersAutomatically = archiveOrdersAutomaticallyDraft
            deleteRemoteOrderDataAutomatically = deleteRemoteOrderDataAutomaticallyDraft
          })
      showSuccessSnack()
    }
  }

  fun saveReserveCheckSettings() {
    viewModelScope.launch {
      userSettingsRepository.saveSettings(
          userSettingsRepository.fetchSettings().copy {
            isReserveCheckEnabled = isReserveCheckEnabledDraft
            reserveCheckPeriodMinutes = reserveCheckPeriodMinutesDraft
          })
      showSuccessSnack()
    }
  }

  fun saveProxySettings() {
    viewModelScope.launch {
      userSettingsRepository.saveSettings(
          userSettingsRepository.fetchSettings().copy {
            isProxyEnabled = isProxyEnabledDraft
            proxyHost = proxyHostDraft
            proxyPort = proxyPortDraft
            preferredProxyType = preferredProxyTypeDraft
          })
      showSuccessSnack()
    }
  }

  private fun showSuccessSnack() {
    SnackbarManager.showMessage(
        SnackbarMessage.from(
            UserMessage.from(R.string.snack_saved_changes_successfully),
            duration = SnackbarDuration.Short,
            withDismissAction = true,
        ))
  }

  fun startDomainCheck() {
    workManager.startDomainCheck()
  }
}
