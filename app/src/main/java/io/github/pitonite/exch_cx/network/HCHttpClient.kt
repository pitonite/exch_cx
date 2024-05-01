package io.github.pitonite.exch_cx.network

import io.github.pitonite.exch_cx.BuildConfig
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.UserSettings
import io.github.pitonite.exch_cx.utils.jsonFormat
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.lang.RuntimeException
import javax.net.ssl.SSLContext
import okhttp3.ConnectionSpec.Companion.CLEARTEXT
import okhttp3.ConnectionSpec.Companion.MODERN_TLS
import okhttp3.ConnectionSpec.Companion.RESTRICTED_TLS
import okhttp3.internal.tls.OkHostnameVerifier

private val connectionSpecs =
    listOf(
        RESTRICTED_TLS, // order matters here, so we put restricted before modern
        MODERN_TLS,
        CLEARTEXT,
    )

private const val TIMEOUT_MILLIS_HIGH = 30_000L
private const val TIMEOUT_MILLIS_LOW = 8_000L

// HC = HealthCheck
private fun createHCHttpClient(
    proxyConfig: ProxyConfig?,
): HttpClient {
  return HttpClient(OkHttp) {
    expectSuccess = false // we only care about the actual response from /connectivity_test endpoint
    followRedirects = false

    engine {
      proxy = proxyConfig
      config {
        if (proxyConfig.isTor()) {
          dns(NoDns())
        }
        hostnameVerifier { hostname, session ->
          session?.sessionContext?.sessionTimeout = 60
          // use default hostname verifier
          OkHostnameVerifier.verify(hostname, session)
        }
        connectionSpecs(connectionSpecs)

        // we trust all certificates since it is irrelevant for this client
        val trustManager = HCX509TrustManager()
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        sslSocketFactory(sslContext.socketFactory, trustManager)
      }
    }

    install(HttpTimeout) {
      if (proxyConfig.isTor()) {
        requestTimeoutMillis = TIMEOUT_MILLIS_HIGH
        connectTimeoutMillis = TIMEOUT_MILLIS_HIGH
        socketTimeoutMillis = TIMEOUT_MILLIS_HIGH
      } else {
        requestTimeoutMillis = TIMEOUT_MILLIS_LOW
        connectTimeoutMillis = TIMEOUT_MILLIS_LOW
        socketTimeoutMillis = TIMEOUT_MILLIS_LOW
      }
    }

    install(UserAgent) { agent = HTTPCLIENT_AGENT }

    defaultRequest {
      headers["X-Requested-With"] = "XMLHttpRequest"
      accept(ContentType.Application.Json)
    }

    install(HttpRequestRetry) {
      retryOnServerErrors(maxRetries = 1)
      exponentialDelay()
    }

    install(ContentNegotiation) { json(jsonFormat) }

    HttpResponseValidator {
      validateResponse { response ->
        val contentType = response.contentType()
        if (contentType?.match(ContentType.Application.Json) != true) {
          throw RuntimeException("response not in json")
        }
      }
    }

    if (BuildConfig.DEBUG) {
      install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.HEADERS
      }
    }
  }
}

class HCHttpClient(settings: UserSettings, private val selectedClearnetDomainIndex: Int) {
  /** Don't use this client directly. Use the provided wrappers to set the required defaults. */
  var _client: HttpClient
  private var proxyConfig: ProxyConfig? = null

  init {
    proxyConfig = getProxyConfig(settings)
    _client = createHCHttpClient(proxyConfig)
  }

  fun HttpRequestBuilder.applyDefaultConfigurations() {
    url {
      protocol = URLProtocol.HTTPS
      host = getExchDomain(PreferredDomainType.NORMAL, selectedClearnetDomainIndex)
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
