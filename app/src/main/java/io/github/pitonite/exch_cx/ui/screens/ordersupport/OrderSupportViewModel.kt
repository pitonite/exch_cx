package io.github.pitonite.exch_cx.ui.screens.ordersupport

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.data.SupportMessagesRepository
import io.github.pitonite.exch_cx.exceptions.toUserMessage
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
@Stable
class OrderSupportViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  private val supportMessagesRepository: SupportMessagesRepository,
) : ViewModel() {

  val orderid = savedStateHandle.getStateFlow(NavArgs.ORDER_ID_KEY, "").apply {
    onEach {
      messageDraft = savedStateHandle.get<String>("$it-messageDraft") ?: ""
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val messages =
      orderid
          .flatMapMerge { supportMessagesRepository.getMessages(it) }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = emptyFlow<PagingData<SupportMessage>>(),
          )

  var sendingWorkState by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  var messageDraft by mutableStateOf("")
    private set

  fun updateMessageDraft(value: String) {
    messageDraft = value
    kotlin.runCatching {
      savedStateHandle.set<String>((orderid.value?:"")+"-messageDraft", value)
    }
  }

  fun sendMessage() {
    if (sendingWorkState.isWorking() || orderid.value == null) return

    sendingWorkState = WorkState.Working()
    val orderid = orderid.value!!
    val message = messageDraft

    viewModelScope.launch {
      try {
        supportMessagesRepository.sendMessage(orderid, message)
        sendingWorkState = WorkState.NotWorking
        updateMessageDraft("")
      } catch (e: Throwable) {
        sendingWorkState = WorkState.Error(e)
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = e.toUserMessage(),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ))
      }
    }
  }
}
