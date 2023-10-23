package io.github.pitonite.exch_cx

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ExchTheme {
        val windowSize = calculateWindowSizeClass(this)

        MainCompose(windowSize)
      }
    }
  }
}
