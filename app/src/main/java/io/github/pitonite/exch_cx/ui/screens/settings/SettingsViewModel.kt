package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.userSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class SettingsViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {

  var apiKeyDraft by mutableStateOf("")
    private set

  var preferredDomainTypeDraft by mutableStateOf(PreferredDomainType.NORMAL)
    private set


  fun updateApiKeyDraft(value: String) {
    apiKeyDraft = value
  }

  fun updatePreferredDomainDraft(value: PreferredDomainType) {
    preferredDomainTypeDraft = value
  }

  fun reloadSettings() {
    viewModelScope.launch {
      userSettingsRepository.userSettingsFlow.first().let {
        apiKeyDraft = it.apiKey
        preferredDomainTypeDraft = it.preferredDomainType
      }
    }
  }

  fun saveChanges() {
    viewModelScope.launch {
      userSettingsRepository.saveSettings(userSettings {
        apiKey = apiKeyDraft
        preferredDomainType = preferredDomainTypeDraft
      })
    }
  }

}