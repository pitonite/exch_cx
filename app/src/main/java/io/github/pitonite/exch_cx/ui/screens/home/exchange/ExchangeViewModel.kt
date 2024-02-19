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
import io.github.pitonite.exch_cx.exceptions.toUserMessage
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.model.api.NetworkFeeOption
import io.github.pitonite.exch_cx.model.api.OrderCreateRequest
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.model.api.XmlRateFee
import io.github.pitonite.exch_cx.model.api.exceptions.ToAddressRequiredException
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.utils.ExchangeWorkState
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.combine
import io.github.pitonite.exch_cx.utils.isWorking
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class ExchangeUiState(
    val fromCurrency: String = "btc",
    val toCurrency: String = "eth",
    val rateFeeMode: RateFeeMode = RateFeeMode.DYNAMIC,
    val rateFee: RateFee? = null,
    val xmlRateFee: XmlRateFee? = null,
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

  private val _xmlRateFee =
      rateFeeRepository
          .findXmlRateStream(_fromCurrency, _toCurrency)
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = null)

  var workState by mutableStateOf<WorkState>(WorkState.NotWorking)
    private set

  val usable =
      snapshotFlow { !workState.isWorking() && _rateFee.value != null }
          .stateIn(
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
    if (workState.isWorking()) return
    workState = ExchangeWorkState.Refreshing
    try {
      rateFeeRepository.updateRateFees(_rateFeeMode.value)
      updateConversionAmounts(CurrencySelection.FROM)
      workState = WorkState.NotWorking
    } catch (e: Throwable) {
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
              _xmlRateFee,
              _networkFeeOption,
          ) { fromCurrency, toCurrency, rateFeeMode, rateFee, xmlRateFee, networkFeeOption ->
            ExchangeUiState(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rateFeeMode = rateFeeMode,
                rateFee = rateFee,
                xmlRateFee = xmlRateFee,
                networkFeeOption = networkFeeOption,
            )
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = ExchangeUiState())

  val currencyList =
      rateFeeRepository.currencies
          .map { it.toPersistentList() }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = persistentListOf(),
          )

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
    val xmlRate = _xmlRateFee.value
    if (fee != null) {
      if (edited == CurrencySelection.FROM) {

        fromAmount.toBigDecimalOrNull()?.let { it ->
          // check against minimum input
          if (xmlRate?.minAmount?.compareTo(it) == 1) {
            updateFromAmount(xmlRate.minAmount.toString())
            SnackbarManager.showMessage(
                SnackbarMessage.from(
                    UserMessage.from(
                        R.string.minimum_input_is, xmlRate.minAmount, fee.fromCurrency),
                    duration = SnackbarDuration.Short,
                    withDismissAction = true,
                ))
            updateConversionAmounts(edited)
            return@let
          }

          // check against maximum input
          if (fee.reserve.compareTo(BigDecimal.ZERO) != 0 &&
              xmlRate?.maxAmount?.compareTo(it) == -1) {
            updateFromAmount(xmlRate.maxAmount.toString())
            SnackbarManager.showMessage(
                SnackbarMessage.from(
                    UserMessage.from(
                        R.string.maximum_input_is, xmlRate.maxAmount, fee.fromCurrency),
                    duration = SnackbarDuration.Short,
                    withDismissAction = true,
                ))
            updateConversionAmounts(edited)
            return@let
          }
          // update to amount
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

  fun setIsReserveCheckTipDismissed(value: Boolean) {
    viewModelScope.launch { userSettingsRepository.setIsReserveCheckTipDismissed(value) }
  }

  fun createOrder(onOrderCreated: (String) -> Unit) {
    if (_rateFee.value == null) return
    if (workState.isWorking()) return
    workState = ExchangeWorkState.CreatingOrder

    val rateFee = _rateFee.value!!
    val feeOption = _networkFeeOption.value
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
                    rateMode = _rateFeeMode.value,
                    fromAmount = fromAmount.toBigDecimalOrNull(),
                    referrerId = null, // TODO
                    aggregation = null, // TODO,
                    rate = rateFee.rate,
                    networkFee = rateFee.networkFee?.let { it[feeOption] } ?: BigDecimal.ZERO,
                    svcFee = rateFee.svcFee,
                ))
        onOrderCreated(orderid)
        workState = WorkState.NotWorking
        reset()
      } catch (e: Throwable) {
        Log.d(TAG, e.message ?: e.toString())

        if (e is ToAddressRequiredException) {
          workState = ExchangeWorkState.ToAddressRequiredError
        } else {
          workState = WorkState.Error(e)
        }

        SnackbarManager.showMessage(
            SnackbarMessage.from(
                message = e.toUserMessage(),
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
