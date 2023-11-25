package io.github.pitonite.exch_cx.network

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.BuildConfig
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.model.api.ErrorResponse
import io.github.pitonite.exch_cx.model.api.exceptions.ApiException
import io.github.pitonite.exch_cx.utils.jsonFormat
import io.github.pitonite.exch_cx.utils.xmlFormat
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
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
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec.Companion.CLEARTEXT
import okhttp3.ConnectionSpec.Companion.MODERN_TLS
import okhttp3.ConnectionSpec.Companion.RESTRICTED_TLS
import okhttp3.Dns
import okhttp3.internal.closeQuietly
import okhttp3.internal.tls.OkHostnameVerifier

private const val NORMAL_HOST = "exch.cx"
private const val ONION_HOST = "hszyoqwrcp7cxlxnqmovp6vjvmnwj33g4wviuxqzq47emieaxjaperyd.onion"
private const val ONION_SSL_FINGERPRINT_SHA256 =
    "1EA14F31FD8A5E2EE1701048CCBC2E01A972BD6FBC3137249539BA5A7A9D2533"
private const val ONION_SSL_FINGERPRINT_SHA1 = "6F7C160C6B5516E4C2E4E648E5636C2D2BFC7309"

fun getExchDomain(preferredDomainType: PreferredDomainType) =
    if (preferredDomainType == PreferredDomainType.ONION) ONION_HOST else NORMAL_HOST

private val connectionSpecs =
    listOf(
        RESTRICTED_TLS, // order matters here, so we put restricted before modern
        MODERN_TLS,
        CLEARTEXT,
    )

private const val TIMEOUT_MILLIS_HIGH = 30_000L
private const val TIMEOUT_MILLIS_LOW = 8_000L

private fun createHttpClient(
    proxyConfig: ProxyConfig?,
    preferredDomainType: PreferredDomainType
): HttpClient {
  return HttpClient(OkHttp) {
    expectSuccess = true // throw on non-2xx

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

        if (preferredDomainType == PreferredDomainType.ONION) {
          val trustManager =
              CustomX509TrustManager(ONION_SSL_FINGERPRINT_SHA256, ONION_SSL_FINGERPRINT_SHA1)
          val sslContext = SSLContext.getInstance("TLS")
          sslContext.init(null, arrayOf(trustManager), null)
          sslSocketFactory(sslContext.socketFactory, trustManager)
          hostnameVerifier { hostname, session ->
            session?.sessionContext?.sessionTimeout = 60
            hostname == ONION_HOST
          }
        }
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

    install(UserAgent) {
      agent = "eXch mobile client/v${BuildConfig.VERSION_NAME}"
    }

    defaultRequest {
      if (!headers.contains("X-Requested-With")) {
        headers["X-Requested-With"] = "XMLHttpRequest"
        accept(ContentType.Application.Json)
      }
    }

    install(HttpCookies)

    install(HttpRequestRetry) {
      retryOnServerErrors(maxRetries = 2)
      exponentialDelay()
    }

    install(ContentNegotiation) {
      json(jsonFormat)

      xml(format = xmlFormat, contentType = ContentType.Text.Html)
    }

    HttpResponseValidator {
      validateResponse { response ->
        val contentType = response.contentType()
        if (contentType?.match(ContentType.Application.Json) == true) {
          try {
            val errorResponse: ErrorResponse = response.body()
            throw ApiException(errorResponse.error)
          } catch (e: ContentConvertException) {
            // no need to handle
          }
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

@Stable
@Singleton
class ExchHttpClient
@Inject
constructor(private val userSettingsRepository: UserSettingsRepository) {
  /** Don't use this client directly. Use the provided wrappers to set the required defaults. */
  var _client: HttpClient
  private var apiKey: String
  private var preferredDomainType: PreferredDomainType
  private var proxyConfig: ProxyConfig? = null

  init {

    runBlocking {
      // for initial load
      val settings = userSettingsRepository.fetchSettings()
      apiKey = settings.apiKey
      preferredDomainType = settings.preferredDomainType
      proxyConfig = getProxyConfig(settings)
      _client = createHttpClient(proxyConfig, preferredDomainType)
    }

    CoroutineScope(Dispatchers.IO).launch {
      // for subsequent updates to settings
      userSettingsRepository.userSettingsFlow.collect {
        apiKey = it.apiKey
        val newPreferredDomainType = it.preferredDomainType != preferredDomainType
        preferredDomainType = it.preferredDomainType
        val newProxyConfig = getProxyConfig(it)
        if (proxyConfig != newProxyConfig || newPreferredDomainType) {
          proxyConfig = newProxyConfig
          _client.closeQuietly()
          _client = createHttpClient(proxyConfig, preferredDomainType)
        }
      }
    }
  }

  fun HttpRequestBuilder.applyDefaultConfigurations() {
    url {
      protocol = URLProtocol.HTTPS
      host = getExchDomain(preferredDomainType)
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

/** Prevent DNS requests. Important when proxying all requests over Tor to not leak DNS queries. */
private class NoDns : Dns {
  override fun lookup(hostname: String): List<InetAddress> {
    return listOf(InetAddress.getByAddress(hostname, ByteArray(4)))
  }
}
