package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.model.api.NetworkFeeOption
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.model.api.XmlRateFee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal

@Stable
class RateFeeRepositoryMock : RateFeeRepository {

  companion object {
    val rates =
        mutableListOf(
            RateFee(
                fromCurrency = "btc",
                toCurrency = "eth",
                rate = "17.340536772394525400".toBigDecimalOrNull()!!,
                rateMode = RateFeeMode.DYNAMIC,
                reserve = "208.680462325551019376".toBigDecimalOrNull()!!,
                svcFee = "0.500000000000000000".toBigDecimalOrNull()!!,
                networkFee =
                    mutableMapOf(
                        NetworkFeeOption.QUICK to "0.00031116".toBigDecimalOrNull()!!,
                        NetworkFeeOption.MEDIUM to "0.00010116".toBigDecimalOrNull()!!,
                    ),
            ),
            RateFee(
                fromCurrency = "eth",
                toCurrency = "btc",
                rate = "0.05709453".toBigDecimalOrNull()!!,
                rateMode = RateFeeMode.DYNAMIC,
                reserve = "25.19396393".toBigDecimalOrNull()!!,
                svcFee = "0.500000000000000000".toBigDecimalOrNull()!!,
                networkFee =
                    mutableMapOf(
                        NetworkFeeOption.QUICK to "0.00004521".toBigDecimalOrNull()!!,
                        NetworkFeeOption.MEDIUM to "0.00000000".toBigDecimalOrNull()!!,
                    ),
            ),
        )

    val xmlRates = mutableListOf(
        XmlRateFee(
            fromCurrency = "eth",
            toCurrency = "btc",
            inAmount = "1.05709453".toBigDecimalOrNull()!!,
            outAmount = "0.05709453".toBigDecimalOrNull()!!,
            availableAmount= "10.70945013".toBigDecimalOrNull()!!,
            minAmount= "0.00709453".toBigDecimalOrNull()!!,
            maxAmount= "1.05709453".toBigDecimalOrNull()!!,
        )
    )
  }

  override val rateFees: StateFlow<List<RateFee>> = MutableStateFlow(rates)
  override val xmlRateFees: StateFlow<List<XmlRateFee>> = MutableStateFlow(xmlRates)

  override val currencies: StateFlow<List<CurrencyDetail>>
    get() {
      val currencyList = mutableMapOf<String, BigDecimal>()
      rates.forEach {
        currencyList[it.toCurrency] = it.reserve
        currencyList.putIfAbsent(it.fromCurrency, BigDecimal.valueOf(0))
      }
      return MutableStateFlow(currencyList.map { (c, r) -> CurrencyDetail(c, r) })
    }

  override suspend fun updateRateFees(feeRateMode: RateFeeMode) {}

  override fun findRateStream(
      fromCurrency: Flow<String?>,
      toCurrency: Flow<String?>
  ): Flow<RateFee?> {
    return combine(fromCurrency, toCurrency) { from, to ->
      rateFees.value.find { it.fromCurrency == from && it.toCurrency == to }
    }
  }

  override fun findXmlRateStream(
    fromCurrency: Flow<String?>,
    toCurrency: Flow<String?>
  ): Flow<XmlRateFee?> {
    return combine(fromCurrency, toCurrency) { from, to ->
      xmlRateFees.value.find { it.fromCurrency == from && it.toCurrency == to }
    }
  }
}
