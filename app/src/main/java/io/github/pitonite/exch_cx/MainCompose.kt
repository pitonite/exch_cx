package io.github.pitonite.exch_cx

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import io.github.pitonite.exch_cx.ui.navigation.ExchNavHost
import io.github.pitonite.exch_cx.utils.ExchNavigationContentPosition
import io.github.pitonite.exch_cx.utils.ExchNavigationType

@Composable
fun MainCompose(
    windowSize: WindowSizeClass,
    deepLinkHandler: DeepLinkHandler,
) {
  /** This will help us select type of navigation depending on window size state of the device. */
  val navigationType: ExchNavigationType

  when (windowSize.widthSizeClass) {
    WindowWidthSizeClass.Compact -> {
      navigationType = ExchNavigationType.BOTTOM_NAVIGATION
    }
    WindowWidthSizeClass.Medium -> {
      navigationType = ExchNavigationType.NAVIGATION_RAIL
    }
    WindowWidthSizeClass.Expanded -> {
      navigationType = ExchNavigationType.PERMANENT_NAVIGATION_DRAWER
    }
    else -> {
      navigationType = ExchNavigationType.BOTTOM_NAVIGATION
    }
  }

  /**
   * Content inside Navigation Rail/Drawer can also be positioned at top, bottom or center for
   * ergonomics and reachability depending upon the height of the device.
   */
  val navigationContentPosition =
      when (windowSize.heightSizeClass) {
        WindowHeightSizeClass.Compact -> {
          ExchNavigationContentPosition.TOP
        }
        WindowHeightSizeClass.Medium,
        WindowHeightSizeClass.Expanded -> {
          ExchNavigationContentPosition.CENTER
        }
        else -> {
          ExchNavigationContentPosition.TOP
        }
      }

  ExchNavHost(navigationType, navigationContentPosition, deepLinkHandler)
}
