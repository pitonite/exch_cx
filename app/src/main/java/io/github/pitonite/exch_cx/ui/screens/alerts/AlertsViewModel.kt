package io.github.pitonite.exch_cx.ui.screens.alerts

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.data.CurrencyReserveTriggerRepository
import io.github.pitonite.exch_cx.data.RateFeeRepository
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@Stable
class AlertsViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val currencyReserveTriggerRepository: CurrencyReserveTriggerRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val rateFeeRepository: RateFeeRepository,
    private val workManager: ExchWorkManager,
) : ViewModel() {

  val userSettings =
      userSettingsRepository.userSettingsFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5_000),
          initialValue = UserSettings.getDefaultInstance(),
      )

  val currencyList =
      rateFeeRepository.rateFees
          .map {
            it.distinctBy { f -> f.toCurrency }
                .map { f -> CurrencyDetail(f.toCurrency, f.reserve) }
                .toPersistentList()
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = persistentListOf(),
          )

  var refreshWorkState by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  var hasNotifPerm by mutableStateOf(false)
    private set

  init {
    if (currencyList.value.isEmpty()) {
      viewModelScope.launch {
        try {
          rateFeeRepository.updateRateFees(RateFeeMode.DYNAMIC)
        } catch (e: Throwable) {
          // do nothing if failed, since ui tells user whats happening
        }
      }
    }
  }

  fun refreshCurrencyList() {
    if (refreshWorkState.isWorking()) return
    refreshWorkState = WorkState.Working()
    viewModelScope.launch {
      try {
        rateFeeRepository.updateRateFees(RateFeeMode.DYNAMIC)
        refreshWorkState = WorkState.NotWorking
      } catch (e: Throwable) {
        refreshWorkState = WorkState.Error(e)
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = UserMessage.from(R.string.snack_network_error),
                withDismissAction = true,
                actionLabelMessage = UserMessage.from(R.string.snack_action_retry),
                duration = SnackbarDuration.Long,
                onSnackbarResult = {
                  if (it == SnackbarResult.ActionPerformed) {
                    refreshCurrencyList()
                  }
                },
            ))
      }
    }
  }

  private val periodicWorkInfo =
      workManager
          .getReserveCheckWorkInfo()
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)

  val reservesCheckWorkState =
      periodicWorkInfo
          .map { periodicInfo ->
            if (periodicInfo?.state == WorkInfo.State.RUNNING) {
              WorkState.Working()
            } else {
              WorkState.NotWorking
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = WorkState.NotWorking,
          )

  val alertsPagingDataFlow: Flow<PagingData<CurrencyReserveTrigger>> =
      currencyReserveTriggerRepository.getTriggersFlow().cachedIn(viewModelScope)

  private fun reEnqueueReserveChecker(immediate: Boolean = true) {
    viewModelScope.launch {
      kotlin.runCatching {
        workManager.adjustReserveCheckWorker(
            userSettingsRepository.fetchSettings(),
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            immediate,
        )
      }
    }
  }

  fun checkReserves() {
    viewModelScope.launch {
      userSettingsRepository.setIsReserveCheckEnabled(true)
      reEnqueueReserveChecker()
    }
  }

  fun stopCheckingReserves() {
    reEnqueueReserveChecker(false)
  }

  private suspend fun checkTriggerCountAndUpdateSettings() {
    val afterUpsertCount = currencyReserveTriggerRepository.count(true)
    if (afterUpsertCount <= 0) {
      userSettingsRepository.setIsReserveCheckEnabled(false)
    } else if (currencyReserveTriggerRepository.count() == 1) {
      // user just inserted the first alert, so we are going to enable reserveCheckAgain
      userSettingsRepository.setIsReserveCheckEnabled(true)
    }
    workManager.adjustReserveCheckWorker(
        userSettingsRepository.fetchSettings(),
        immediate = false,
    )
  }

  fun upsertTrigger(trigger: CurrencyReserveTrigger) {
    viewModelScope.launch {
      currencyReserveTriggerRepository.upsertTrigger(trigger)
      checkTriggerCountAndUpdateSettings()
    }
  }

  fun deleteTrigger(trigger: CurrencyReserveTrigger) {
    viewModelScope.launch {
      try {
        currencyReserveTriggerRepository.deleteTrigger(trigger.id)
        checkTriggerCountAndUpdateSettings()
      } catch (e: Throwable) {
        // doesn't matter much
        // very unlikely to throw.
      }
    }
  }

  fun checkPermissions(context: Context) {
    hasNotifPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }
}
