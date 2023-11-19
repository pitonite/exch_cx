package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.model.api.XmlRateFee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface RateFeeRepository {

  val rateFees: StateFlow<List<RateFee>>

  val xmlRateFees: StateFlow<List<XmlRateFee>>

  /** list of supported currencies in lower case */
  val currencies: StateFlow<List<CurrencyDetail>>

  suspend fun updateRateFees(feeRateMode: RateFeeMode)

  fun findRateStream(fromCurrency: Flow<String?>, toCurrency: Flow<String?>): Flow<RateFee?>

  fun findXmlRateStream(fromCurrency: Flow<String?>, toCurrency: Flow<String?>): Flow<XmlRateFee?>
}
