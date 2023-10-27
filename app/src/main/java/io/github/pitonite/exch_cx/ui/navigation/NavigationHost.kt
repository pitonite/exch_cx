package io.github.pitonite.exch_cx.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import io.github.pitonite.exch_cx.utils.ExchNavigationContentPosition
import io.github.pitonite.exch_cx.utils.ExchNavigationType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchNavHost(
    navigationType: ExchNavigationType,
    navigationContentPosition: ExchNavigationContentPosition
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val exchNavController = rememberExchNavController()
  val navController = exchNavController.navController

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val selectedDestination =
      navBackStackEntry?.destination?.route ?: PrimaryDestinations.EXCHANGE.route

  if (navigationType == ExchNavigationType.PERMANENT_NAVIGATION_DRAWER) {
    // TODO check on custom width of PermanentNavigationDrawer: b/232495216
    // TODO check compose Reply app when material is out of alpha for the todo above
    PermanentNavigationDrawer(
        drawerContent = {
          PermanentNavigationDrawerContent(
              selectedDestination = selectedDestination,
              navigationContentPosition = navigationContentPosition,
              navigateTo = exchNavController::navigateToTopLevel,
          )
        }) {
          ExchAppContent(
              exchNavController,
              navigationType,
              selectedDestination,
              navigationContentPosition,
              onDrawerClicked = { scope.launch { drawerState.close() } },
          )
        }
  } else {
    ModalNavigationDrawer(
        drawerContent = {
          ModalNavigationDrawerContent(
              selectedDestination = selectedDestination,
              navigationContentPosition = navigationContentPosition,
              navigateTo = exchNavController::navigateToTopLevel,
              onDrawerClicked = { scope.launch { drawerState.close() } },
          )
        },
        drawerState = drawerState,
    ) {
      ExchAppContent(
          exchNavController,
          navigationType,
          selectedDestination,
          navigationContentPosition,
          onDrawerClicked = { scope.launch { drawerState.close() } },
      )
    }
  }
}

@Composable
private fun ExchAppContent(
    exchNavController: ExchNavController,
    navigationType: ExchNavigationType,
    selectedDestination: String,
    navigationContentPosition: ExchNavigationContentPosition,
    modifier: Modifier = Modifier,
    onDrawerClicked: () -> Unit = {}
) {

  Row(modifier = modifier.fillMaxSize()) {
    AnimatedVisibility(visible = navigationType == ExchNavigationType.NAVIGATION_RAIL) {
      ExchNavigationRail(
          selectedDestination = selectedDestination,
          navigationContentPosition = navigationContentPosition,
          navigateTo = exchNavController::navigateToTopLevel,
          onDrawerClicked = onDrawerClicked,
      )
    }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
      Column(
          Modifier.weight(1f, true).fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        NavHost(
            navController = exchNavController.navController,
            startDestination = PrimaryDestinations.EXCHANGE.route,
            contentAlignment = Alignment.Center,
            modifier = Modifier.widthIn(0.dp, 560.dp).weight(1f, true).fillMaxWidth(),
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = {
              scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            },
            popExitTransition = { scaleOutOfContainer() },
        ) {
          exchNavGraph(
              exchNavController = exchNavController,
              navigateTo = exchNavController::navigateTo,
              onOrderSelected = exchNavController::navigateToOrderDetail,
              modifier = Modifier.weight(1f, true))
        }
      }

      AnimatedVisibility(
          navigationType == ExchNavigationType.BOTTOM_NAVIGATION &&
              PrimaryDestinations.entries.any { selectedDestination.startsWith(it.route) }) {
            ExchBottomNavigationBar(
                selectedDestination = selectedDestination,
                navigateTo = exchNavController::navigateToTopLevel,
            )
          }
    }
  }
}
