package io.github.pitonite.exch_cx.ui.screens.orderdetail

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.copy
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.exceptions.toUserMessage
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.codified.enums.codifiedEnum
import io.github.pitonite.exch_cx.utils.isWorking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

val InvalidOrder =
    Order(
        id = "",
        fromCurrency = "",
        toCurrency = "",
        rate = BigDecimal.ZERO,
        rateMode = RateFeeMode.DYNAMIC,
        svcFee = BigDecimal.ZERO,
        toAddress = "",
        state = OrderState.CREATED.codifiedEnum(),
        minInput = BigDecimal.ZERO,
        maxInput = BigDecimal.ONE,
    )

@HiltViewModel
@Stable
class OrderDetailViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

  val orderid = savedStateHandle.getStateFlow<String?>(NavArgs.ORDER_ID_KEY, null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val order =
      orderid
          .flatMapMerge { it?.let { orderRepository.getOrder(it) } ?: flow { emit(InvalidOrder) } }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = InvalidOrder,
          )

  val userSettings =
      userSettingsRepository.userSettingsFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5000),
          initialValue =
              UserSettings.getDefaultInstance().copy { hasShownOrderBackgroundUpdateNotice = true })

  var refreshWorkState by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  var submitNewToAddressWorkState by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  fun refreshOrder() {
    if (refreshWorkState.isWorking() || orderid.value == null) return

    refreshWorkState = WorkState.Working()
    val id = orderid.value!!
    val hasLetterOfGuarantee = !order.value?.letterOfGuarantee.isNullOrEmpty()
    val hasLetterOfGuaranteeConditions = order.value?.stateError == null && order.value?.state?.knownOrNull() != OrderState.CREATED

    viewModelScope.launch {
      try {
        orderRepository.fetchAndUpdateOrder(id)
        if (!hasLetterOfGuarantee && hasLetterOfGuaranteeConditions) {
            try {
              orderRepository.fetchAndUpdateLetterOfGuarantee(id)
            } catch (e: Exception) {
              // no need
            }
        }
        refreshWorkState = WorkState.NotWorking
      } catch (e: Throwable) {
        refreshWorkState = WorkState.Error(e)
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = e.toUserMessage(),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ))
      }
    }
  }

  fun submitNewToAddress(newAddress: String) {
    if (submitNewToAddressWorkState.isWorking()) return
    submitNewToAddressWorkState = WorkState.Working()
    val orderid = orderid.value!!

    viewModelScope.launch {
      try {
        orderRepository.revalidateAddress(orderid, newAddress)
        refreshOrder()
        submitNewToAddressWorkState = WorkState.NotWorking
      } catch (e: Throwable) {
        submitNewToAddressWorkState = WorkState.Error(e)
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = e.toUserMessage(),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ))
      }
    }
  }

  fun toggleArchive() {
    val order = order.value ?: return
    viewModelScope.launch {
      try {
        orderRepository.setArchive(order.id, !order.archived)
      } catch (e: Throwable) {
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = UserMessage.from(e.message ?: e.toString()),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ))
      }
    }
  }

  fun onAutomaticDialogResult(result: Boolean) {
    viewModelScope.launch {
      userSettingsRepository.setHasShownOrderBackgroundUpdateNotice(true)
      userSettingsRepository.setIsOrderAutoUpdateEnabled(result)
    }
  }
}
