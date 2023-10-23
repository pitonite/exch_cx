package io.github.pitonite.exch_cx.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.model.RateFeesObjectTransformer
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import nl.adaptivity.xmlutil.serialization.XML
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {

  @OptIn(ExperimentalSerializationApi::class)
  @Singleton
  @Provides
  fun getHttpClient(userSettingsRepository: UserSettingsRepository): HttpClient {
    var apiKey: String
    var preferredDomain: PreferredDomainType
    runBlocking {
      val settings = userSettingsRepository.fetchSettings()
      apiKey = settings.apiKey
      preferredDomain = settings.preferredDomainType
    }

    val client =
        HttpClient(Android) {
          expectSuccess = true // throw on non-2xx

          //      install(UserAgent) {
          //        agent = "Ktor client"
          //      }
          BrowserUserAgent()

          defaultRequest {
            url {
              protocol = URLProtocol.HTTPS
              if (preferredDomain == PreferredDomainType.ONION) {
                host = "hszyoqwrcp7cxlxnqmovp6vjvmnwj33g4wviuxqzq47emieaxjaperyd.onion"
              } else {
                host = "exch.cx"
              }
              if (apiKey.isNotEmpty()) {
                parameters.append("api_key", apiKey)
              }
            }
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
    return client
  }
}
