package io.github.pitonite.exch_cx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberExchNavController(
    navController: NavHostController = rememberNavController()
): ExchNavController = remember(navController) { ExchNavController(navController) }

@Stable
class ExchNavController(
    val navController: NavHostController,
) {
  private val currentRoute: String?
    get() = navController.currentDestination?.route

  fun upPress() {
    navController.navigateUp()
  }

  fun popBackStack(): Boolean {
    return navController.popBackStack()
  }

  fun navigateToTopLevel(route: String) {
    navigateTo(route, true)
  }

  fun navigateTo(route: String, popUpToStart: Boolean = false) {
    if (route != currentRoute) {
      navController.navigate(route) {
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
        // Pop up backstack to the first destination and save state. This makes going back
        // to the start destination when pressing back in any other route
        if (popUpToStart) {
          popUpTo(findStartDestination(navController.graph).id) { saveState = true }
        }
      }
    }
  }

  fun navigateToOrderDetail(orderId: String, from: NavBackStackEntry) {
    // In order to discard duplicated navigation events, we check the Lifecycle
    if (from.lifecycleIsResumed()) {
      navController.navigate("${SecondaryDestinations.ORDER_DETAIL_ROUTE}/$orderId")
    }
  }
}

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED

private val NavGraph.startDestination: NavDestination?
  get() = findNode(startDestinationId)

/**
 * Copied from similar function in NavigationUI.kt
 *
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation/navigation-ui/src/main/java/androidx/navigation/ui/NavigationUI.kt
 */
private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
  return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
}
