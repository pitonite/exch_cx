package io.github.pitonite.exch_cx.utils

/** Different type of navigation supported by app depending on device size and state. */
enum class ExchNavigationType {
  BOTTOM_NAVIGATION,
  NAVIGATION_RAIL,
  PERMANENT_NAVIGATION_DRAWER
}

/**
 * Different position of navigation content inside Navigation Rail, Navigation Drawer depending on
 * device size and state.
 */
enum class ExchNavigationContentPosition {
  TOP,
  CENTER
}
