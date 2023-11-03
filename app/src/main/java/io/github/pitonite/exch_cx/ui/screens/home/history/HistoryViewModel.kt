package io.github.pitonite.exch_cx.ui.screens.home.history

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.room.Order
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
@Stable
class HistoryViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

  val orderPagingDataFlow: Flow<PagingData<Order>> =
      orderRepository.getOrderList(true).cachedIn(viewModelScope)
}
