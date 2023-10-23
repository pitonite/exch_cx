package io.github.pitonite.exch_cx.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

interface IRateFeeRepository {

  val rateFees: StateFlow<List<RateFee>>

  /** list of supported currencies in lower case */
  val currencies: StateFlow<List<CurrencyDetail>>

  suspend fun updateRateFees(feeRateMode: RateFeeMode)

  fun findRateStream(fromCurrency: Flow<String?>, toCurrency: Flow<String?>): Flow<RateFee?>
}

@Singleton
class RateFeeRepository @Inject constructor(private val httpClient: HttpClient) :
    IRateFeeRepository {

  private val _rateFees = MutableStateFlow(emptyList<RateFee>())
  private val _currencies = MutableStateFlow(emptyList<CurrencyDetail>())

  override val rateFees: StateFlow<List<RateFee>>
    get() = _rateFees

  override val currencies: StateFlow<List<CurrencyDetail>>
    get() = _currencies

  override suspend fun updateRateFees(feeRateMode: RateFeeMode) {
    val resp =
        httpClient
            .get("/api/rates") {
              url { parameters.append("rate_mode", feeRateMode.name.lowercase()) }
            }
            .body<RateFeeResponse>()
    _rateFees.update { resp.rateFees }
    val currencyList = mutableMapOf<String, BigDecimal>()
    resp.rateFees.forEach {
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
  abstract fun provideRateFeeRepository(rateFeeRepository: RateFeeRepository): IRateFeeRepository
}
