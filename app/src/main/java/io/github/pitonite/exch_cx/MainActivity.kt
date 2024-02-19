package io.github.pitonite.exch_cx

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.ui.components.ProvideSnackbarHostState
import io.github.pitonite.exch_cx.ui.components.SnackbarMessageHandler
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  @Inject lateinit var userSettingsRepository: UserSettingsRepository

  @Inject lateinit var deepLinkHandler: DeepLinkHandler

  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    // Handle the splash screen transition.
    // must be called before super.onCreate()
    installSplashScreen()
    super.onCreate(savedInstanceState)

    // to preload settings asynchronously, to runBlocking later in di/HttpClientModule
    lifecycleScope.launch {
      val settings = userSettingsRepository.fetchSettings()

      // setup initial settings
      if (!settings.firstInitDone) {
        userSettingsRepository.saveSettings(
            settings.copy {
              firstInitDone = true
              archiveOrdersAutomatically = true
              proxyHost = "127.0.0.1"
              proxyPort = "9050"
            },
        )
      }
    }

    setContent {
      val windowSize = calculateWindowSizeClass(this)
      ExchTheme {
        SnackbarMessageHandler()

        ProvideSnackbarHostState() {
          Box(
              Modifier.safeDrawingPadding(),
          ) {
            MainCompose(windowSize, deepLinkHandler)
          }
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    deepLinkHandler.handleDeepLink(intent)
    // consume the deeplink
    intent = null
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    deepLinkHandler.handleDeepLink(intent)
  }
}
