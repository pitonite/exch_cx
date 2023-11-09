package io.github.pitonite.exch_cx.ui.screens.home.exchange

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.copy
import io.github.pitonite.exch_cx.data.OrderRepository
import io.github.pitonite.exch_cx.data.RateFeeRepository
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.model.api.NetworkFeeOption
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.utils.ExchangeWorkState
import io.github.pitonite.exch_cx.utils.WorkState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

@Immutable
data class ExchangeUiState(
    val fromCurrency: String = "btc",
    val toCurrency: String = "eth",
    val rateFeeMode: RateFeeMode = RateFeeMode.DYNAMIC,
    val rateFee: RateFee? = null,
    val svcFee: BigDecimal? = null,
    val networkFeeOption: NetworkFeeOption? = null,
)

@HiltViewModel
@Stable
@OptIn(ExperimentalMaterial3Api::class)
class ExchangeViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val rateFeeRepository: RateFeeRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

  companion object {
    const val TAG = "ExchangeViewModel"
  }

  val userSettings =
      userSettingsRepository.userSettingsFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5_000),
          initialValue = UserSettings.getDefaultInstance().copy { isExchangeTipDismissed = true })

  private val _fromCurrency: MutableStateFlow<String> = MutableStateFlow("btc")
  private val _networkFeeOption: MutableStateFlow<NetworkFeeOption?> =
      MutableStateFlow(NetworkFeeOption.QUICK)
  private val _toCurrency: MutableStateFlow<String> = MutableStateFlow("eth")
  private val _rateFeeMode: MutableStateFlow<RateFeeMode> = MutableStateFlow(RateFeeMode.DYNAMIC)
  private val _rateFee =
      rateFeeRepository
          .findRateStream(_fromCurrency, _toCurrency)
          .onEach {
            if (it?.networkFee != null) {
              if (it.networkFee[NetworkFeeOption.QUICK] != null) {
                _networkFeeOption.value = NetworkFeeOption.QUICK
              } else {
                _networkFeeOption.value = it.networkFee.keys.first()
              }
            } else {
              _networkFeeOption.value = null
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)

  var workState by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  val busy = snapshotFlow { WorkState.isWorking(workState) }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = false)

  init {
    viewModelScope.launch {
      _rateFeeMode
          .onEach { _updateFeeRates() }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.Eagerly,
              initialValue = RateFeeMode.DYNAMIC)
    }
  }

  private suspend fun _updateFeeRates() {
    if (workState == WorkState.Working) return
    workState = ExchangeWorkState.Refreshing
    try {
      rateFeeRepository.updateRateFees(_rateFeeMode.value)
      updateConversionAmounts(CurrencySelection.FROM)
      workState = WorkState.NotWorking
    } catch (e: Exception) {
      workState = WorkState.Error(e)
      SnackbarManager.showMessage(
          SnackbarMessage.from(
              message = UserMessage.from(R.string.snack_network_error),
              withDismissAction = true,
              actionLabelMessage = UserMessage.from(R.string.snack_action_retry),
              duration = SnackbarDuration.Long,
              onSnackbarResult = {
                if (it == SnackbarResult.ActionPerformed) {
                  updateFeeRates()
                }
              },
          ))
    }
  }

  fun updateFeeRates() {
    viewModelScope.launch { _updateFeeRates() }
  }

  val uiState: StateFlow<ExchangeUiState> =
      combine(
              _fromCurrency,
              _toCurrency,
              _rateFeeMode,
              _rateFee,
              _networkFeeOption,
          ) { fromCurrency, toCurrency, rateFeeMode, rateFee, networkFeeOption ->
            ExchangeUiState(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rateFeeMode = rateFeeMode,
                rateFee = rateFee,
                networkFeeOption = networkFeeOption,
            )
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = ExchangeUiState())

  var fromAmount by mutableStateOf("")
    private set

  var toAmount by mutableStateOf("")
    private set

  var toAddress by mutableStateOf("")
    private set

  var refundAddress by mutableStateOf("")
    private set

  fun updateFromAmount(input: String) {
    fromAmount = input
  }

  fun updateToAmount(input: String) {
    toAmount = input
  }

  fun updateToAddress(input: String) {
    toAddress = input
  }

  fun updateRefundAddress(input: String) {
    refundAddress = input
  }

  fun updateFromCurrency(currency: String) {
    _fromCurrency.value = currency
    updateConversionAmounts(CurrencySelection.FROM)
  }

  fun updateToCurrency(currency: String) {
    _toCurrency.value = currency
    updateConversionAmounts(CurrencySelection.FROM) // Still From because of UX
  }

  fun updateFeeRateMode(feeMode: RateFeeMode) {
    _rateFeeMode.value = feeMode
  }

  fun updateConversionAmounts(edited: CurrencySelection) {
    val fee = _rateFee.value
    if (fee != null) {
      if (edited == CurrencySelection.FROM) {
        // update to amount
        fromAmount.toBigDecimalOrNull()?.let { it ->
          if (it > BigDecimal.ZERO) { // we don't want infinity
            val newToAmount =
                it.multiply(fee.rate, MathContext.DECIMAL64)
                    .let networkLet@{
                      if (!fee.networkFee.isNullOrEmpty()) {
                        val networkFee = fee.networkFee[_networkFeeOption.value]!!
                        return@networkLet it.minus(networkFee)
                      }
                      return@networkLet it
                    }
                    .setScale(18, RoundingMode.CEILING)
            updateToAmount(newToAmount.stripTrailingZeros().toString())
          } else {
            updateToAmount("")
          }
        }
      } else {
        // update from amount
        toAmount.toBigDecimalOrNull()?.let {
          if (it > BigDecimal.ZERO) {
            val newFromAmount =
                it.let networkLet@{
                      if (!fee.networkFee.isNullOrEmpty()) {
                        val networkFee = fee.networkFee[_networkFeeOption.value]!!
                        return@networkLet it.plus(networkFee)
                      }
                      return@networkLet it
                    }
                    .divide(fee.rate, MathContext.DECIMAL64)
                    .setScale(18, RoundingMode.CEILING)
            updateFromAmount(newFromAmount.stripTrailingZeros().toString())
          } else {
            updateToAmount("")
          }
        }
      }
    } else {
      val predicate: (rate: RateFee) -> Boolean =
          if (edited == CurrencySelection.FROM) {
            { rate -> rate.fromCurrency == _fromCurrency.value }
          } else {
            { rate -> rate.toCurrency == _toCurrency.value }
          }
      rateFeeRepository.rateFees.value.find(predicate)?.let {
        if (edited == CurrencySelection.FROM) {
          _toCurrency.value = it.toCurrency
        } else {
          _fromCurrency.value = it.fromCurrency
        }
      }
    }
  }

  fun swapCurrencies() {
    val tmpCurr = _fromCurrency.value
    _fromCurrency.value = _toCurrency.value
    _toCurrency.value = tmpCurr
    val tmpAmount = toAmount
    toAmount = fromAmount
    fromAmount = tmpAmount
    updateConversionAmounts(CurrencySelection.FROM)
  }

  fun updateNetworkFeeOption(networkFeeOption: NetworkFeeOption?) {
    _networkFeeOption.value = networkFeeOption
    updateConversionAmounts(CurrencySelection.FROM)
  }

  fun setIsExchangeTipDismissed(value: Boolean) {
    viewModelScope.launch { userSettingsRepository.setExchangeTipDismissed(value) }
  }

  fun createOrder(onOrderCreated: (String) -> Unit) {
    if (_rateFee.value == null) return
    if (workState == WorkState.Working) return
    workState = ExchangeWorkState.CreatingOrder

    val rate = _rateFee.value!!
    viewModelScope.launch {
      if (_rateFee.value == null) return@launch
      try {
        val orderid =
            orderRepository.createOrder(
                OrderCreateRequest(
                    fromCurrency = _fromCurrency.value,
                    toCurrency = _toCurrency.value,
                    toAddress = toAddress,
                    refundAddress = if (refundAddress.isNullOrEmpty()) null else refundAddress,
                    feeOption = _networkFeeOption.value,
                    calculatedFromAmount = fromAmount.toBigDecimalOrNull(),
                    calculatedToAmount = toAmount.toBigDecimalOrNull(),
                    rateMode = _rateFeeMode.value,
                    referrerId = null, // TODO
                    aggregation = null, // TODO
                ),
                rate)
        onOrderCreated(orderid)
        workState = WorkState.NotWorking
        reset()
      } catch (e: Exception) {
        Log.d(TAG, e.message ?: e.toString())
        workState = WorkState.Error(e)
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = UserMessage.from(e.message ?: e.toString()),
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            ))
      }
    }
  }

  private fun reset() {
    val defaultState = ExchangeUiState()
    _fromCurrency.value = defaultState.fromCurrency
    _toCurrency.value = defaultState.toCurrency
    fromAmount = ""
    toAmount = ""
    toAddress = ""
    refundAddress = ""
  }
}
