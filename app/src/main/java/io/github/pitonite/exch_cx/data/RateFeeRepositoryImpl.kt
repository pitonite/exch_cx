package io.github.pitonite.exch_cx.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.model.api.RateFeeResponse
import io.github.pitonite.exch_cx.utils.ExchParser
import io.github.pitonite.exch_cx.utils.ParsedRate
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

// TODO: remove when duplicate call is removed
private fun mergeRates(
    rateFees: List<RateFee>,
    homeRateFees: PersistentList<ParsedRate>
): List<RateFee> {
  val rateMap = mutableMapOf<String, BigDecimal>()
  for (rate in rateFees) {
    rateMap[rate.fromCurrency + "_" + rate.toCurrency] = rate.rate
  }
  for (rate in homeRateFees) {
    val key = rate.fromCurrency + "_" + rate.toCurrency
    if (rateMap.contains(key)) {
      rateMap[rate.fromCurrency + "_" + rate.toCurrency] = rate.rate
    } else {
      rateMap.remove(key)
    }
  }
  return rateFees.map { it.copy(rate = rateMap[it.fromCurrency + "_" + it.toCurrency]!!) }
}
// end of todo

@Singleton
class RateFeeRepositoryImpl @Inject constructor(private val httpClient: ExchHttpClient) :
    RateFeeRepository {

  private val _rateFees = MutableStateFlow(emptyList<RateFee>())
  private val _currencies = MutableStateFlow(emptyList<CurrencyDetail>())

  override val rateFees: StateFlow<List<RateFee>>
    get() = _rateFees

  override val currencies: StateFlow<List<CurrencyDetail>>
    get() = _currencies

  override suspend fun updateRateFees(feeRateMode: RateFeeMode) {
    var rates =
        httpClient
            .get("/api/rates") {
              url { parameters.append("rate_mode", feeRateMode.name.lowercase()) }
            }
            .body<RateFeeResponse>()
            .rateFees
    // TODO: remove duplicate call when api is fixed
    val homeResp = httpClient.get("/") { headers["X-Requested-With"] = "" }
    if (homeResp.status != HttpStatusCode.OK) {
      throw RuntimeException("website is unavailable")
    }
    val homeBody = homeResp.bodyAsText()
    val parsedRates =
        ExchParser.parseRates(homeBody) ?: throw RuntimeException("home page parsing had a problem")

    rates = mergeRates(rates, parsedRates)
    // end of duplicate call

    _rateFees.update { rates }
    val currencyList = mutableMapOf<String, BigDecimal>()
    rates.forEach {
      currencyList[it.toCurrency.lowercase()] = it.reserve
      currencyList.putIfAbsent(it.fromCurrency.lowercase(), BigDecimal.valueOf(0))
    }
    _currencies.update { currencyList.map { (c, r) -> CurrencyDetail(c, r) } }
  }

  override fun findRateStream(
      fromCurrency: Flow<String?>,
      toCurrency: Flow<String?>
  ): Flow<RateFee?> {
    return combine(_rateFees, fromCurrency, toCurrency) { rateFees, from, to ->
      rateFees.find {
        (if (!from.isNullOrEmpty()) it.fromCurrency == from else true) &&
            (if (!to.isNullOrEmpty()) it.toCurrency == to else true)
      }
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RateFeeRepositoryModule {
  @Binds
  @Singleton
  abstract fun provideRateFeeRepository(
      rateFeeRepositoryImpl: RateFeeRepositoryImpl
  ): RateFeeRepository
}
