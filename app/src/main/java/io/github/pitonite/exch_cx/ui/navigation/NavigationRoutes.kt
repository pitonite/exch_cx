package io.github.pitonite.exch_cx.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.screens.home.exchange.Exchange
import io.github.pitonite.exch_cx.ui.screens.home.exchange.ExchangeViewModel
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelect
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelectViewModel
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.ui.screens.home.history.History
import io.github.pitonite.exch_cx.ui.screens.home.history.HistoryViewModel
import io.github.pitonite.exch_cx.ui.screens.home.orders.Orders
import io.github.pitonite.exch_cx.ui.screens.home.orders.OrdersViewModel
import io.github.pitonite.exch_cx.ui.screens.orderdetail.OrderDetail
import io.github.pitonite.exch_cx.ui.screens.settings.Settings
import io.github.pitonite.exch_cx.ui.screens.settings.SettingsViewModel
import io.github.pitonite.exch_cx.utils.enumByNameIgnoreCase
import io.github.pitonite.exch_cx.utils.sharedViewModel

enum class PrimaryDestinations(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
  EXCHANGE(R.string.exchange, Icons.Default.CurrencyExchange, "exchange"),
  ORDERS(R.string.orders, Icons.Default.PendingActions, "orders"),
  HISTORY(R.string.history, Icons.Default.History, "history"),
}

enum class ExchangeSections(@StringRes val title: Int, val route: String) {
  AMOUNTS(R.string.exchange, "exchange/amounts"),
  ADDRESS(R.string.details, "exchange/address"),
  OVERVIEW(R.string.overview, "exchange/overview"),
  CURRENCY_SELECT(R.string.select_currency, "exchange/currency_select"),
}

object SecondaryDestinations {
  const val ORDER_DETAIL_ROUTE = "order"
  const val SETTINGS_ROUTE = "settings"
}

object NavArgs {
  const val SELECTION_KEY = "selection"
  const val ORDER_ID_KEY = "orderId"
}

fun NavGraphBuilder.exchNavGraph(
    exchNavController: ExchNavController,
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  addExchangeGraph(
      exchNavController, onOrderSelected, navigateTo, exchNavController::upPress, modifier)
  addOrders(onOrderSelected, navigateTo, modifier)
  addHistory(onOrderSelected, navigateTo, modifier)
  addOrderDetail(navigateTo, exchNavController::upPress, modifier)
  addSettings(exchNavController::upPress, modifier)
}

private fun NavGraphBuilder.addExchangeGraph(
    exchNavController: ExchNavController,
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    upPress: () -> Unit,
    modifier: Modifier = Modifier
) {
  navigation(
      route = PrimaryDestinations.EXCHANGE.route,
      startDestination = ExchangeSections.AMOUNTS.route) {
        composable(ExchangeSections.AMOUNTS.route) { backStackEntry ->
          val viewModel =
              backStackEntry.sharedViewModel<ExchangeViewModel>(exchNavController.navController)

          Exchange(
              viewModel = viewModel,
              onOrderCreated = { id -> onOrderSelected(id, backStackEntry) },
              onNavigateToRoute = onNavigateToRoute,
              modifier = modifier)
        }

        composable(
            route =
                "${ExchangeSections.CURRENCY_SELECT.route}?${NavArgs.SELECTION_KEY}={${NavArgs.SELECTION_KEY}}",
            arguments =
                listOf(
                    navArgument(NavArgs.SELECTION_KEY) {
                      type = NavType.StringType
                      defaultValue = "FROM"
                    },
                ),
        ) { backStackEntry ->
          val exchangeViewModel =
              backStackEntry.sharedViewModel<ExchangeViewModel>(exchNavController.navController)

          val viewModel = hiltViewModel<CurrencySelectViewModel>()

          val selection =
              enumByNameIgnoreCase<CurrencySelection>(
                  backStackEntry.arguments?.getString(NavArgs.SELECTION_KEY) ?: "")!!

          CurrencySelect(
              viewModel = viewModel,
              exchangeViewModel = exchangeViewModel,
              modifier = modifier,
              upPress = upPress,
              currencySelection = selection,
          )
        }
      }
}

private fun NavGraphBuilder.addOrders(
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

  composable(PrimaryDestinations.ORDERS.route) { from ->
    val viewModel = hiltViewModel<OrdersViewModel>()
    Orders(
        viewModel = viewModel,
        onOrderSelected = { id -> onOrderSelected(id, from) },
        onNavigateToRoute = onNavigateToRoute,
        modifier = modifier,
    )
  }
}

private fun NavGraphBuilder.addHistory(
    onOrderSelected: (String, NavBackStackEntry) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

  composable(PrimaryDestinations.HISTORY.route) { from ->
    val viewModel = hiltViewModel<HistoryViewModel>()
    History(
        viewModel = viewModel,
        onOrderSelected = { id -> onOrderSelected(id, from) },
        onNavigateToRoute,
        modifier)
  }
}

private fun NavGraphBuilder.addOrderDetail(
    onNavigateToRoute: (String) -> Unit,
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
  composable(
      "${SecondaryDestinations.ORDER_DETAIL_ROUTE}/{${NavArgs.ORDER_ID_KEY}}",
      arguments = listOf(navArgument(NavArgs.ORDER_ID_KEY) { type = NavType.StringType })) {
          backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val orderId = arguments.getString(NavArgs.ORDER_ID_KEY)
        if (orderId.isNullOrEmpty()) {
          upPress()
        } else {
          OrderDetail(orderId, onNavigateToRoute, modifier)
        }
      }
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
