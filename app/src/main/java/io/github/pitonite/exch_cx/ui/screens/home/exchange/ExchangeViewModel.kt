package io.github.pitonite.exch_cx.ui.screens.home.exchange

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pitonite.exch_cx.data.IRateFeeRepository
import io.github.pitonite.exch_cx.data.NetworkFeeChoice
import io.github.pitonite.exch_cx.data.RateFee
import io.github.pitonite.exch_cx.data.RateFeeMode
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.utils.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val networkFeeChoice: NetworkFeeChoice? = null,
    val working: Boolean = true,
)

@HiltViewModel
class ExchangeViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val rateFeeRepository: IRateFeeRepository,
) : ViewModel() {

  private val _fromCurrency: MutableStateFlow<String> = MutableStateFlow("btc")
  private val _networkFeeChoice: MutableStateFlow<NetworkFeeChoice?> =
      MutableStateFlow(NetworkFeeChoice.QUICK)
  private val _toCurrency: MutableStateFlow<String> = MutableStateFlow("eth")
  private val _rateFeeMode: MutableStateFlow<RateFeeMode> = MutableStateFlow(RateFeeMode.DYNAMIC)
  private val _rateFee =
      rateFeeRepository
          .findRateStream(_fromCurrency, _toCurrency)
          .onEach {
            if (it?.networkFee != null) {
              _networkFeeChoice.value = NetworkFeeChoice.QUICK
            } else {
              _networkFeeChoice.value = null
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)
  private val _working: MutableStateFlow<Boolean> = MutableStateFlow(true)

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
    _working.value = true
    rateFeeRepository.updateRateFees(_rateFeeMode.value)
    updateConversionAmounts(CurrencySelection.FROM)
    _working.value = false
  }

  fun updateFeeRates() {
    viewModelScope.launch { _updateFeeRates() }
  }

  val uiState: StateFlow<ExchangeUiState> =
      combine(_fromCurrency, _toCurrency, _rateFeeMode, _rateFee, _networkFeeChoice, _working) {
              fromCurrency,
              toCurrency,
              rateFeeMode,
              rateFee,
              networkFeeChoice,
              working ->
            ExchangeUiState(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rateFeeMode = rateFeeMode,
                rateFee = rateFee,
                networkFeeChoice = networkFeeChoice,
                working = working,
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

  fun updateFromAmount(input: String) {
    fromAmount = input
  }

  fun updateToAmount(input: String) {
    toAmount = input
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
      val svcFeeMultiplier =
          BigDecimal.ONE -
              fee.svcFee.divide(
                  BigDecimal.valueOf(100),
                  MathContext.DECIMAL64) // since svcFee returned from api is in percent (1-100)

      if (edited == CurrencySelection.FROM) {
        // update to amount
        fromAmount.toBigDecimalOrNull()?.let { it ->
          if (it > BigDecimal.ZERO) { // we don't want infinity
            val newToAmount =
                it.multiply(fee.rate, MathContext.DECIMAL64)
                    .multiply(svcFeeMultiplier, MathContext.DECIMAL64)
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
            val svcFeeRevertMultiplier =
                BigDecimal.ONE.divide(svcFeeMultiplier, MathContext.DECIMAL64)
            val newFromAmount =
                it.multiply(svcFeeRevertMultiplier, MathContext.DECIMAL64)
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

  fun updateWorking(newState: Boolean) {
    _working.value = newState
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

  fun updateNetworkFeeChoice(networkFeeChoice: NetworkFeeChoice?) {
    _networkFeeChoice.value = networkFeeChoice
  }
}
