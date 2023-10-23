package io.github.pitonite.exch_cx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavHostController,
): VM {
  val navGraphRoute = destination.parent?.route ?: return hiltViewModel<VM>()
  val parentEntry = remember(this) { navController.getBackStackEntry(navGraphRoute) }
  return hiltViewModel<VM>(parentEntry)
}
