package io.github.pitonite.exch_cx.ui.screens.home.orders

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
import io.github.pitonite.exch_cx.CurrentWorkProgress
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.TotalWorkItems
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrdersViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val workManager: ExchWorkManager
) : ViewModel() {

  private val periodicWorkInfo =
      workManager
          .getAutoUpdateWorkInfo()
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)

  private val oneTimeWorkInfo =
      workManager
          .getOneTimeOrderUpdateWorkInfo()
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)

  val autoUpdateWorkState =
      periodicWorkInfo
          .combine(oneTimeWorkInfo) { periodicInfo, oneTimeInfo ->
            if (oneTimeInfo?.state == WorkInfo.State.RUNNING ) {
              WorkState.Working(
                  currentWorkProgress = oneTimeInfo.progress.getInt(CurrentWorkProgress, 0),
                  totalWorkItems = oneTimeInfo.progress.getInt(TotalWorkItems, 0),
                  )
            } else if (periodicInfo?.state == WorkInfo.State.RUNNING) {
              WorkState.Working(
                  currentWorkProgress = periodicInfo.progress.getInt(CurrentWorkProgress, 0),
                  totalWorkItems = periodicInfo.progress.getInt(TotalWorkItems, 0),
              )
            }  else {
              WorkState.NotWorking
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = WorkState.NotWorking,
          )

  var importOrderWork by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  var showImportDialog by mutableStateOf(false)
    private set

  val orderPagingDataFlow: Flow<PagingData<Order>> =
      orderRepository.getOrderList(false).cachedIn(viewModelScope)

  private fun cancelAndReEnqueueAutoUpdater() {
    viewModelScope.launch {
      kotlin.runCatching {
        workManager.adjustAutoUpdater(
            userSettingsRepository.fetchSettings(), ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
      }
    }
  }

  fun updateOrders() {
    if (autoUpdateWorkState.value.isWorking()) return
    cancelAndReEnqueueAutoUpdater()
    workManager.startOneTimeOrderUpdate()
  }

  fun stopUpdatingOrders() {
    if (periodicWorkInfo.value?.state == WorkInfo.State.RUNNING) {
      cancelAndReEnqueueAutoUpdater()
    } else {
      workManager.stopOneTimeOrderUpdate()
    }
  }

  fun showImportOrderDialog() {
    showImportDialog = true
  }

  fun onImportOrderPressed(orderid: String) {
    if (importOrderWork.isWorking()) return
    importOrderWork = WorkState.Working()
    viewModelScope.launch {
      try {
        if (orderRepository.fetchAndUpdateOrder(orderid)) {
          SnackbarManager.showMessage(
              snackbarMessage =
                  SnackbarMessage.from(message = UserMessage.from(R.string.import_order_existed)))
        }
        showImportDialog = false
        importOrderWork = WorkState.NotWorking
      } catch (err: Throwable) {
        importOrderWork = WorkState.Error(err)
      }
    }
  }

  fun onDismissImportDialogRequest() {
    if (importOrderWork.isWorking()) return
    importOrderWork = WorkState.NotWorking
    showImportDialog = false
  }
}
