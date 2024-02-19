package io.github.pitonite.exch_cx.ui.screens.alerts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.pitonite.exch_cx.ExchWorkManager
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.CurrencyReserveTriggerRepositoryMock
import io.github.pitonite.exch_cx.data.RateFeeRepositoryMock
import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import io.github.pitonite.exch_cx.exceptions.toUserMessage
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.components.StopProgress
import io.github.pitonite.exch_cx.ui.screens.alerts.components.AlertAddEditDialog
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.isWorking
import io.github.pitonite.exch_cx.utils.verticalFadingEdge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Alerts(viewModel: AlertsViewModel, upPress: () -> Unit, modifier: Modifier = Modifier) {

  val alertsPagingItems = viewModel.alertsPagingDataFlow.collectAsLazyPagingItems()
  val reservesCheckWorkState by viewModel.reservesCheckWorkState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
  val currencyList by viewModel.currencyList.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
  val context = LocalContext.current
  var showAddEditDialog by remember { mutableStateOf(false) }
  var editingTrigger by remember { mutableStateOf<CurrencyReserveTrigger?>(null) }

  val permActivityLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.checkPermissions(context)
      }

  val permLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.checkPermissions(context)
        if (!it &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, Manifest.permission.POST_NOTIFICATIONS)) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
              permActivityLauncher.launch(
                  Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                  },
              )
            } catch (e: Throwable) {
              SnackbarManager.showMessage(
                  SnackbarMessage.from(
                      message = UserMessage.from(R.string.snack_activity_launch_error),
                      withDismissAction = true,
                      duration = SnackbarDuration.Short,
                  ),
              )
            }
          }
        }
      }

  LaunchedEffect(alertsPagingItems.loadState.refresh) {
    if (alertsPagingItems.loadState.refresh is LoadState.Error) {
      SnackbarManager.showMessage(
          SnackbarMessage.from(
              message =
                  (alertsPagingItems.loadState.refresh as LoadState.Error).error.toUserMessage(),
          ),
      )
    }
  }

  LaunchedEffect(lifecycleState) {
    when (lifecycleState) {
      Lifecycle.State.STARTED,
      Lifecycle.State.RESUMED -> {
        viewModel.checkPermissions(context)
      }
      else -> {}
    }
  }

  Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      snackbarHost = { SnackbarHost(hostState = SnackbarManager.snackbarHostState) },
      topBar = {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                ),
            title = { Text(stringResource(R.string.alerts)) },
        )
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = {
              editingTrigger = null
              showAddEditDialog = true
            },
        ) {
          Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.label_add_alert))
        }
      },
  ) { padding ->
    Box(
        modifier =
            modifier
                .padding(padding)
                .padding(horizontal = dimensionResource(R.dimen.page_padding))
                .fillMaxSize(),
    ) {
      if (!viewModel.hasNotifPerm) {
        BasicAlertDialog(
            onDismissRequest = { upPress() },
        ) {
          Card() {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = modifier.padding(dimensionResource(R.dimen.padding_lg)),
            ) {
              Text(
                  stringResource(R.string.alerts_notification_perm_dialog),
                  textAlign = TextAlign.Center,
              )
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Button(
                    onClick = { permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                ) {
                  Text(stringResource(R.string.grant_permission))
                }
              }
            }
          }
        }
      }

      if (showAddEditDialog) {
        AlertAddEditDialog(
            onDismissRequest = { showAddEditDialog = false },
            editingTrigger = editingTrigger,
            currencyList = currencyList,
            refreshWorkState = viewModel.refreshWorkState,
            onRefresh = { viewModel.refreshCurrencyList() },
        ) {
          viewModel.upsertTrigger(it)
          showAddEditDialog = false
        }
      }
      when (alertsPagingItems.loadState.refresh) {
        is LoadState.Error ->
            Card {
              Column(Modifier.padding(vertical = 70.dp, horizontal = 20.dp)) {
                Text(stringResource(R.string.unknown_error))
              }
            }
        else -> {
          if (alertsPagingItems.itemCount == 0) {
            Column {
              Card {
                Column(
                    Modifier.padding(
                        horizontal = dimensionResource(R.dimen.padding_md),
                        vertical = 70.dp,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                      stringResource(R.string.notice_empty_alerts),
                      fontSize = 22.sp,
                      textAlign = TextAlign.Center,
                  )
                  Spacer(Modifier.height(50.dp))
                  val addAlertHintText = buildAnnotatedString {
                    append(stringResource(R.string.hint_add_alert))
                    append(" (")
                    appendInlineContent("icon", "([add alert icon])")
                    append(")")
                  }
                  Text(
                      text = addAlertHintText,
                      fontSize = 22.sp,
                      textAlign = TextAlign.Center,
                      inlineContent =
                          mapOf(
                              Pair(
                                  "icon",
                                  InlineTextContent(
                                      Placeholder(
                                          width = 22.sp,
                                          height = 22.sp,
                                          placeholderVerticalAlign =
                                              PlaceholderVerticalAlign.Center,
                                      ),
                                  ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription =
                                            stringResource(R.string.label_add_alert),
                                    )
                                  },
                              ),
                          ),
                  )
                }
              }
            }
          } else {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier.fillMaxSize()
                        .verticalFadingEdge(listState, dimensionResource(R.dimen.fading_edge)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
            ) {
              item {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.page_padding))) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.Center,
                  ) {
                    if (!reservesCheckWorkState.isWorking()) {
                      OutlinedButton(onClick = viewModel::checkReserves) {
                        Text(stringResource(R.string.check_now))
                      }
                    } else {
                      StopProgress(
                          onClick = viewModel::stopCheckingReserves,
                          stringResource(R.string.label_stop_checking_reserves),
                      )
                    }
                  }
                }
              }

              items(
                  count = alertsPagingItems.itemCount,
                  key = alertsPagingItems.itemKey { it.id },
              ) { index ->
                val trigger = alertsPagingItems[index]
                if (trigger != null) {
                  AlertItem(
                      trigger,
                      onEdit = {
                        editingTrigger = trigger
                        showAddEditDialog = true
                      },
                      onToggle = {
                        viewModel.upsertTrigger(trigger.copy(isEnabled = !trigger.isEnabled))
                      },
                      onDelete = { viewModel.deleteTrigger(trigger) },
                      onToggleOnlyOnce = {
                        viewModel.upsertTrigger(trigger.copy(onlyOnce = !trigger.onlyOnce))
                      },
                  )
                }
              }
              item {
                if (alertsPagingItems.loadState.append == LoadState.Loading) {
                  CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Preview("default")
@Composable
fun AlertsPreview() {
  ExchTheme {
    Alerts(
        viewModel =
            AlertsViewModel(
                savedStateHandle = SavedStateHandle(),
                currencyReserveTriggerRepository = CurrencyReserveTriggerRepositoryMock(),
                workManager = ExchWorkManager(LocalContext.current),
                userSettingsRepository = UserSettingsRepositoryMock(),
                rateFeeRepository = RateFeeRepositoryMock(),
            ),
        upPress = {},
    )
  }
}
