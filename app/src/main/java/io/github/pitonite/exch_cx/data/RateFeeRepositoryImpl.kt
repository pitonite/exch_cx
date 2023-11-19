package io.github.pitonite.exch_cx.data

import androidx.compose.runtime.Stable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.di.ExchHttpClient
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.model.api.RateFee
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.model.api.RateFeeResponse
import io.github.pitonite.exch_cx.model.api.XmlRateFee
import io.github.pitonite.exch_cx.model.api.XmlRateFeesResponse
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@Singleton
@Stable
class RateFeeRepositoryImpl @Inject constructor(private val httpClient: ExchHttpClient) :
    RateFeeRepository {

  private val _rateFees = MutableStateFlow(emptyList<RateFee>())
  private val _xmlRateFees = MutableStateFlow(emptyList<XmlRateFee>())
  private val _currencies = MutableStateFlow(emptyList<CurrencyDetail>())

  override val rateFees: StateFlow<List<RateFee>>
    get() = _rateFees

  override val xmlRateFees: StateFlow<List<XmlRateFee>>
    get() = _xmlRateFees

  override val currencies: StateFlow<List<CurrencyDetail>>
    get() = _currencies

  override suspend fun updateRateFees(feeRateMode: RateFeeMode) = coroutineScope {
    val xmlRates =
        async(
            CoroutineExceptionHandler { _, thrown ->
              // we degrade the experience instead.
            }) {
              httpClient
                  .get("/rates.xml") {
                    headers["X-Requested-With"] = "XMLHttpRequest"
                    accept(ContentType.Text.Html)
                  }
                  .body<XmlRateFeesResponse>()
                  .rateFees
                  .map {
                    it.copy(
                        fromCurrency = it.fromCurrency.lowercase(),
                        toCurrency = it.toCurrency.lowercase())
                  }
            }

    val rates = async {
      httpClient
          .get("/api/rates") {
            url { parameters.append("rate_mode", feeRateMode.name.lowercase()) }
          }
          .body<RateFeeResponse>()
          .rateFees
    }

    _xmlRateFees.value = xmlRates.await()
    _rateFees.value = rates.await()

    val currencyList = mutableMapOf<String, BigDecimal>()
    _rateFees.value.forEach {
      currencyList[it.toCurrency.lowercase()] = it.reserve
      currencyList.putIfAbsent(it.fromCurrency.lowercase(), BigDecimal.valueOf(0))
    }
    _currencies.value = currencyList.map { (c, r) -> CurrencyDetail(c, r) }
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

  override fun findXmlRateStream(
      fromCurrency: Flow<String?>,
      toCurrency: Flow<String?>
  ): Flow<XmlRateFee?> {
    return combine(_xmlRateFees, fromCurrency, toCurrency) { rateFees, from, to ->
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
