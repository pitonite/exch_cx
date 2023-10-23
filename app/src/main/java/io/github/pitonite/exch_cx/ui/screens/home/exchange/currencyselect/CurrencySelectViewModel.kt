package io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.data.IRateFeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CurrencySelectViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val feeRepository: IRateFeeRepository,
) : ViewModel() {

  var searchTerm by mutableStateOf("")
    private set

  val currencyListState =
      snapshotFlow { searchTerm }
          .combine(feeRepository.currencies) { search, currencies ->
            if (search.isNotEmpty()) currencies.filter { it.name.contains(search) } else currencies
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = emptyList(),
          )

  fun updateSearchTerm(newTerm: String) {
    searchTerm = newTerm
  }
}
