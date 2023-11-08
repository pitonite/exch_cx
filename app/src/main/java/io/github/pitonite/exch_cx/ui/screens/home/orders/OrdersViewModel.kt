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
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.utils.WorkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrdersViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

  var refreshing by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  var importOrderWork by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  var showImportDialog by mutableStateOf(false)
    private set

  val orderPagingDataFlow: Flow<PagingData<Order>> =
      orderRepository.getOrderList(false).cachedIn(viewModelScope)

  fun updateOrders() {
    // todo

    // cancel and reschedule the periodic work
    // then start a one time work and wait for it's result
  }

  fun showImportOrderDialog() {
    showImportDialog = true
  }

  fun onImportOrderPressed(orderId: String) {
    if (importOrderWork == WorkState.Working) return

    importOrderWork = WorkState.Working
    viewModelScope.launch {
      try {
        if (orderRepository.fetchAndUpdateOrder(orderId)) {
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
    if (importOrderWork == WorkState.Working) return
    importOrderWork = WorkState.NotWorking
    showImportDialog = false
  }
}
