package io.github.pitonite.exch_cx.ui.screens.orderdetail

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock
import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.di.getExchDomain
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.RefreshButton
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.createNotificationChannels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetail(
    viewModel: OrderDetailViewModel,
    upPress: () -> Unit,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current
  val order by viewModel.order.collectAsStateWithLifecycle()
  val settings by viewModel.userSettings.collectAsStateWithLifecycle()

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
          createNotificationChannels(context)
        }
      }

  Scaffold(
      snackbarHost = { SnackbarHost(hostState = SnackbarManager.snackbarHostState) },
      topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(),
            title = {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.order))
                Text(
                    viewModel.orderId.value ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp)
              }
            },
            navigationIcon = {
              IconButton(onClick = { upPress() }) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                )
              }
            },
            actions = {
              if (order != null) {
                RefreshButton(
                    onClick = { viewModel.refreshOrder() },
                    enabled = viewModel.refreshWorkState != WorkState.Working,
                    refreshing = viewModel.refreshWorkState == WorkState.Working,
                )

                var showMenu by remember { mutableStateOf(false) }

                IconButton(onClick = { showMenu = !showMenu }) {
                  Icon(
                      imageVector = Icons.Default.MoreVert,
                      contentDescription = stringResource(R.string.label_more))
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                  DropdownMenuItem(
                      leadingIcon = {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                        )
                      },
                      text = { Text(stringResource(R.string.label_open_in_browser)) },
                      onClick = {
                        uriHandler.openUri(
                            "https://${getExchDomain(settings.preferredDomainType)}/order/${order!!.id}")
                      })

                  DropdownMenuItem(
                      leadingIcon = {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector =
                                if (order!!.archived) Icons.Default.Unarchive
                                else Icons.Default.Archive,
                            contentDescription = null,
                        )
                      },
                      text = {
                        Text(
                            if (order!!.archived) stringResource(R.string.label_unarchive)
                            else stringResource(R.string.label_archive))
                      },
                      onClick = { viewModel.toggleArchive() })
                }
              }
            },
        )
      }) { padding ->
        Column(
            modifier
                .padding(padding)
                .padding(horizontal = dimensionResource(R.dimen.page_padding))
                .verticalScroll(rememberScrollState()),
        ) {
          AutomaticOrderUpdateDialog(show = !settings.hasShownOrderBackgroundUpdateNotice) {
            if (it) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
              }
            }
            viewModel.onAutomaticDialogResult(it)
          }
          if (order != null) {
            OrderColumn(order!!)
          } else {
            Card {
              Column(
                  Modifier.padding(
                      horizontal = dimensionResource(R.dimen.padding_md),
                      vertical = 70.dp,
                  ),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(stringResource(R.string.notice_order_details_not_found))
              }
            }
          }
        }
      }
}

@Composable
fun OrderColumn(order: Order) {
  Column {
    // TODO: Show order details
  }
}

@Preview("detail column")
@Composable
fun OrderColumnPreview() {
  ExchTheme { OrderColumn(OrderRepositoryMock.orders[0]) }
}

@Preview("default")
@Composable
fun OrderDetailPreview() {
  val savedStateHandle = SavedStateHandle()
  savedStateHandle[NavArgs.ORDER_ID_KEY] = "ee902b8a5fe0844d41"
  ExchTheme {
    OrderDetail(
        viewModel =
            OrderDetailViewModel(
                savedStateHandle,
                OrderRepositoryMock(),
                UserSettingsRepositoryMock(),
            ),
        upPress = {},
    )
  }
}
