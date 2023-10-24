package io.github.pitonite.exch_cx.di

import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.model.RateFeesObjectTransformer
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import nl.adaptivity.xmlutil.serialization.XML
import javax.inject.Inject
import javax.inject.Singleton

private const val NORMAL_HOST = "exch.cx"
private const val ONION_HOST = "hszyoqwrcp7cxlxnqmovp6vjvmnwj33g4wviuxqzq47emieaxjaperyd.onion"

@OptIn(ExperimentalSerializationApi::class)
@Singleton
class ExchHttpClient
@Inject
constructor(private val userSettingsRepository: UserSettingsRepository) {
  /** Don't use this client directly. Use the provided wrappers to set the required defaults. */
  val _client: HttpClient
  private var apiKey: String
  private var preferredDomainType: PreferredDomainType

  init {
    CoroutineScope(Dispatchers.Main).launch {
      userSettingsRepository.userSettingsFlow.collect {
        apiKey = it.apiKey
        preferredDomainType = it.preferredDomainType
      }
    }

    runBlocking {
      // for initial load
      val settings = userSettingsRepository.fetchSettings()
      apiKey = settings.apiKey
      preferredDomainType = settings.preferredDomainType
    }

    _client =
        HttpClient(Android) {
          expectSuccess = true // throw on non-2xx

          install(HttpTimeout) {
            requestTimeoutMillis = 8_000
            connectTimeoutMillis = 8_000
            socketTimeoutMillis = 8_000
          }

          //      install(UserAgent) {
          //        agent = "Ktor client"
          //      }
          BrowserUserAgent()

          defaultRequest {
            header("X-Requested-With", "XMLHttpRequest")
            accept(ContentType.Application.Json)
          }

          install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
          }

          install(ContentNegotiation) {
            json(
                Json {
                  isLenient = true
                  ignoreUnknownKeys = true
                  decodeEnumsCaseInsensitive = true
                  explicitNulls = false
                  serializersModule = SerializersModule { contextual(RateFeesObjectTransformer) }
                })

            xml(
                format =
                    XML {
                      autoPolymorphic = true
                      repairNamespaces = true
                    })
          }
        }
  }

  fun HttpRequestBuilder.applyDefaultConfigurations() {
    url {
      protocol = URLProtocol.HTTPS
      host = if (preferredDomainType == PreferredDomainType.ONION) ONION_HOST else NORMAL_HOST
      if (apiKey.isNotEmpty()) {
        parameters["api_key"] = apiKey
      }
    }
  }

  suspend inline fun get(crossinline block: HttpRequestBuilder.() -> Unit): HttpResponse {
    return this._client.get {
      applyDefaultConfigurations()
      this.apply(block)
    }
  }

  suspend inline fun get(
      url: Url,
      crossinline block: HttpRequestBuilder.() -> Unit = {}
  ): HttpResponse {
    return this._client.get(url) {
      applyDefaultConfigurations()
      this.apply(block)
    }
  }

  suspend inline fun get(
      urlString: String,
      crossinline block: HttpRequestBuilder.() -> Unit = {}
  ): HttpResponse {
    return this._client.get(urlString) {
      applyDefaultConfigurations()
      this.apply(block)
    }
  }
}
