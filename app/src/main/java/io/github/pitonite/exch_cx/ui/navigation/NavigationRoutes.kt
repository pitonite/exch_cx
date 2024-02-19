package io.github.pitonite.exch_cx.ui.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.screens.alerts.Alerts
import io.github.pitonite.exch_cx.ui.screens.alerts.AlertsViewModel
import io.github.pitonite.exch_cx.ui.screens.home.exchange.Exchange
import io.github.pitonite.exch_cx.ui.screens.home.exchange.ExchangeViewModel
import io.github.pitonite.exch_cx.ui.screens.home.history.History
import io.github.pitonite.exch_cx.ui.screens.home.history.HistoryViewModel
import io.github.pitonite.exch_cx.ui.screens.home.orders.Orders
import io.github.pitonite.exch_cx.ui.screens.home.orders.OrdersViewModel
import io.github.pitonite.exch_cx.ui.screens.orderdetail.OrderDetail
import io.github.pitonite.exch_cx.ui.screens.orderdetail.OrderDetailViewModel
import io.github.pitonite.exch_cx.ui.screens.ordersupport.OrderSupport
import io.github.pitonite.exch_cx.ui.screens.ordersupport.OrderSupportViewModel
import io.github.pitonite.exch_cx.ui.screens.settings.Settings
import io.github.pitonite.exch_cx.ui.screens.settings.SettingsViewModel
import io.github.pitonite.exch_cx.utils.sharedViewModel

const val EXCH_APP_SCHEME = "exchcx"

enum class PrimaryDestinations(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
  EXCHANGE(R.string.exchange, Icons.Default.CurrencyExchange, "exchange"),
  ORDERS(R.string.orders, Icons.Default.PendingActions, "orders"),
  HISTORY(R.string.history, Icons.Default.History, "history"),
}

object SecondaryDestinations {
  const val ORDER_DETAIL_ROUTE = "order"
  const val ORDER_SUPPORT_ROUTE = "order_support"
  const val SETTINGS_ROUTE = "settings"
  const val ALERTS_ROUTE = "alerts"
}

object NavArgs {
  const val ORDER_ID_KEY = "orderid"
}

fun NavGraphBuilder.exchNavGraph(
    exchNavController: ExchNavController,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  addExchangeGraph(
      exchNavController, exchNavController::navigateToOrderDetail, navigateTo, modifier)
  addOrders(exchNavController::navigateToOrderDetail, modifier)
  addHistory(exchNavController::navigateToOrderDetail, modifier)
  addOrderDetail(exchNavController::upPress, exchNavController::navigateToOrderSupport, modifier)
  addOrderSupport(exchNavController::upPress, modifier)
  addSettings(exchNavController::upPress, modifier)
  addAlerts(exchNavController::upPress, modifier)
}

private fun NavGraphBuilder.addExchangeGraph(
    exchNavController: ExchNavController,
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  composable(PrimaryDestinations.EXCHANGE.route) { backStackEntry ->
    val viewModel =
        backStackEntry.sharedViewModel<ExchangeViewModel>(exchNavController.navController)

    Exchange(
        viewModel = viewModel,
        onNavigateToRoute = onNavigateToRoute,
        onOrderSelected = { id -> onOrderSelected(id, backStackEntry) },
        modifier = modifier)
  }
}

private fun NavGraphBuilder.addOrders(
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    modifier: Modifier = Modifier,
) {

  composable(PrimaryDestinations.ORDERS.route) { from ->
    val viewModel = hiltViewModel<OrdersViewModel>()
    Orders(
        viewModel = viewModel,
        onOrderSelected = { id -> onOrderSelected(id, from) },
        modifier = modifier,
    )
  }
}

private fun NavGraphBuilder.addHistory(
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    modifier: Modifier = Modifier,
) {

  composable(PrimaryDestinations.HISTORY.route) { from ->
    val viewModel = hiltViewModel<HistoryViewModel>()
    History(viewModel = viewModel, onOrderSelected = { id -> onOrderSelected(id, from) }, modifier)
  }
}

private fun NavGraphBuilder.addOrderDetail(
    upPress: () -> Unit,
    navigateToOrderSupport: (String, NavBackStackEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
  composable(
      "${SecondaryDestinations.ORDER_DETAIL_ROUTE}/{${NavArgs.ORDER_ID_KEY}}",
      deepLinks =
          listOf(
              navDeepLink {
                uriPattern =
                    "$EXCH_APP_SCHEME://${SecondaryDestinations.ORDER_DETAIL_ROUTE}/{${NavArgs.ORDER_ID_KEY}}"
              }),
      arguments = listOf(navArgument(NavArgs.ORDER_ID_KEY) { type = NavType.StringType })) {
          backStackEntry ->
        val orderid = backStackEntry.arguments?.getString(NavArgs.ORDER_ID_KEY)
        if (orderid.isNullOrEmpty()) {
          upPress()
        } else {
          val viewModel = hiltViewModel<OrderDetailViewModel>()
          OrderDetail(
              viewModel = viewModel,
              upPress = upPress,
              navigateToOrderSupport = { navigateToOrderSupport(it, backStackEntry) },
              modifier = modifier,
          )
        }
      }
}

fun getOrderDetailUri(orderid: String): Uri {
  return "$EXCH_APP_SCHEME://${SecondaryDestinations.ORDER_DETAIL_ROUTE}/${orderid}".toUri()
}

private fun NavGraphBuilder.addSettings(
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
  composable(SecondaryDestinations.SETTINGS_ROUTE) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    Settings(viewModel, upPress, modifier)
  }
}

private fun NavGraphBuilder.addAlerts(
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
  composable(SecondaryDestinations.ALERTS_ROUTE) {
    val viewModel = hiltViewModel<AlertsViewModel>()
    Alerts(viewModel, upPress, modifier)
  }
}

private fun NavGraphBuilder.addOrderSupport(
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
  composable(
      "${SecondaryDestinations.ORDER_SUPPORT_ROUTE}/{${NavArgs.ORDER_ID_KEY}}",
      deepLinks =
          listOf(
              navDeepLink {
                uriPattern =
                    "$EXCH_APP_SCHEME://${SecondaryDestinations.ORDER_SUPPORT_ROUTE}/{${NavArgs.ORDER_ID_KEY}}"
              }),
      arguments = listOf(navArgument(NavArgs.ORDER_ID_KEY) { type = NavType.StringType })) {
          backStackEntry ->
        val orderid = backStackEntry.arguments?.getString(NavArgs.ORDER_ID_KEY)
        if (orderid.isNullOrEmpty()) {
          upPress()
        } else {
          val viewModel = hiltViewModel<OrderSupportViewModel>()
          OrderSupport(
              viewModel = viewModel,
              upPress = upPress,
              modifier = modifier,
          )
        }
      }
}

fun getOrderSupportUri(orderid: String): Uri {
  return "$EXCH_APP_SCHEME://${SecondaryDestinations.ORDER_SUPPORT_ROUTE}/${orderid}".toUri()
}
