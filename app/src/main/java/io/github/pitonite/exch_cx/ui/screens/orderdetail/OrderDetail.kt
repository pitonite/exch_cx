package io.github.pitonite.exch_cx.ui.screens.orderdetail

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock
import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.data.room.GENERATING_FROM_ADDRESS
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.di.getExchDomain
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.getTranslation
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.ClickableText
import io.github.pitonite.exch_cx.ui.components.Notice
import io.github.pitonite.exch_cx.ui.components.RefreshButton
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.ui.screens.home.orders.ExchangePairRow
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.codified.enums.toLocalizedString
import io.github.pitonite.exch_cx.utils.copyToClipboard
import io.github.pitonite.exch_cx.utils.createNotificationChannels
import io.github.pitonite.exch_cx.utils.rememberQrBitmapPainter
import io.github.pitonite.exch_cx.utils.verticalFadingEdge
import java.math.BigDecimal
import java.math.RoundingMode

// TODO: remove if network indicator was provided by the api
val etheriumBasedCoins = "eth|dai|usdt|usdc".toRegex(RegexOption.IGNORE_CASE)

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
  val scrollState = rememberScrollState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
          createNotificationChannels(context)
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
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseOnSurface),
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
                .verticalFadingEdge(scrollState, dimensionResource(R.dimen.fading_edge))
                .padding(horizontal = dimensionResource(R.dimen.page_padding))
                .verticalScroll(scrollState),
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
  val context = LocalContext.current

  Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding))) {
    ExchangePairRow(fromCurrency = order.fromCurrency, toCurrency = order.toCurrency)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              "1 ${order.fromCurrency.uppercase()} = ${order.rate} ${order.toCurrency.uppercase()}",
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
              "${stringResource(R.string.label_mode)} ${order.rateMode.getTranslation()} [${order.svcFee.setScale(1, RoundingMode.HALF_UP)}%]",
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

    if (order.stateError != null) {
      Card(isError = true) {
        Column(
            modifier =
                Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                    .padding(
                        vertical = dimensionResource(R.dimen.padding_xl),
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
          Text(
              order.stateError.toLocalizedString(),
              color = MaterialTheme.colorScheme.onErrorContainer)
        }
      }
    }

    when (order.state.knownOrNull()) {
      OrderState.CANCELLED -> {

        Card(isError = true) {
          Column(
              modifier =
                  Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                      .padding(
                          vertical = dimensionResource(R.dimen.padding_xl),
                      ),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
          ) {
            Text(
                stringResource(R.string.error_order_cancelled),
                color = MaterialTheme.colorScheme.onErrorContainer)
          }
        }
      }
      OrderState.CREATED -> {}
      OrderState.AWAITING_INPUT -> {

        if (order.maxInput.compareTo(BigDecimal.ZERO) == 0) {
          Column(
              modifier =
                  Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                      .padding(
                          vertical = dimensionResource(R.dimen.padding_lg),
                      ),
              verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_lg)),
          ) {
            Text(stringResource(R.string.order_state_max_input_zero))
          }
        } else {

          Card {
            Column(
                modifier =
                    Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                        .padding(
                            vertical = dimensionResource(R.dimen.padding_lg),
                        ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_lg)),
            ) {
              Text(
                  stringResource(
                      R.string.order_state_title_created, order.fromCurrency.uppercase()),
                  style = MaterialTheme.typography.headlineSmall)

              Text(
                  stringResource(R.string.label_minimum) +
                      " ${order.minInput} ${order.fromCurrency.uppercase()}")
              Text(
                  stringResource(R.string.label_maximum) +
                      " ${order.maxInput} ${order.fromCurrency.uppercase()}")
              Spacer(Modifier)

              Text(stringResource(R.string.label_to_order_address, order.fromCurrency.uppercase()))

              if (order.fromAddr != GENERATING_FROM_ADDRESS) {
                val annotatedString = buildAnnotatedString {
                  withStyle(
                      style =
                          SpanStyle(
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                              fontSize = MaterialTheme.typography.titleLarge.fontSize),
                  ) {
                    append(order.fromAddr)
                  }
                  append("  ")
                  pushStringAnnotation(tag = "copy_icon", annotation = "copy_icon")
                  appendInlineContent("copy_icon", "([copy address])")
                  pop()
                }
                val iconSize = LocalTextStyle.current.fontSize.times(1.5f)

                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                      annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { span
                        ->
                        if (span.tag == "copy_icon") {
                          copyToClipboard(
                              context,
                              order.fromAddr,
                              confirmationMessage = R.string.snack_address_copied)
                        }
                      }
                    },
                    inlineContent =
                        mapOf(
                            Pair(
                                "copy_icon",
                                InlineTextContent(
                                    Placeholder(
                                        width = iconSize,
                                        height = iconSize,
                                        placeholderVerticalAlign =
                                            PlaceholderVerticalAlign.Center)) {
                                      Icon(
                                          Icons.Default.ContentCopy,
                                          contentDescription =
                                              stringResource(R.string.label_copy_address))
                                    })),
                )

                if (order.stateError == null &&
                    etheriumBasedCoins.containsMatchIn(order.fromCurrency)) {
                  Notice(stringResource(R.string.notice_etherium_based_coins))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                  Image(
                      painter =
                          rememberQrBitmapPainter(
                              content = order.fromAddr, size = 300.dp, padding = 1.dp),
                      contentDescription =
                          stringResource(R.string.desc_send_qrcode_image, order.fromCurrency),
                      modifier = Modifier.clip(MaterialTheme.shapes.large),
                  )
                }
              } else {
                Text(
                    stringResource(R.string.address_generating),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                )
              }
            }
          }
        }
      }
      else -> {
        // TODO: implement unknown state enum
      }
    }

    Card {
      Column(
          modifier =
              Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                  .padding(
                      vertical = dimensionResource(R.dimen.padding_lg),
                  ),
          verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_lg)),
      ) {
        Column {
          Text(stringResource(R.string.label_exchanged_amount_to_address))

          val annotatedString = buildAnnotatedString {
            withStyle(
                style =
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            ) {
              append(order.toAddress)
            }
            append("  ")
            pushStringAnnotation(tag = "copy_icon", annotation = "copy_icon")
            appendInlineContent("copy_icon", "([copy address])")
            pop()
          }
          val iconSize = LocalTextStyle.current.fontSize.times(1.5f)

          ClickableText(
              text = annotatedString,
              onClick = { offset ->
                annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { span ->
                  if (span.tag == "copy_icon") {
                    copyToClipboard(
                        context,
                        order.toAddress,
                        confirmationMessage = R.string.snack_address_copied)
                  }
                }
              },
              inlineContent =
                  mapOf(
                      Pair(
                          "copy_icon",
                          InlineTextContent(
                              Placeholder(
                                  width = iconSize,
                                  height = iconSize,
                                  placeholderVerticalAlign = PlaceholderVerticalAlign.Center)) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription =
                                        stringResource(R.string.label_copy_address))
                              })))
        }
      }
    }

    Spacer(Modifier)
  }
}

@Preview("order - created", widthDp = 360)
@Composable
fun OrderColumnCreatedPreview() {
  ExchTheme { Surface { OrderColumn(OrderRepositoryMock.orders[0]) } }
}

@Preview("order - created - max zero", widthDp = 360)
@Composable
fun OrderColumnCreatedMaxZeroPreview() {
  ExchTheme { Surface { OrderColumn(OrderRepositoryMock.orders[1]) } }
}

@Preview("order - created - error address", widthDp = 360)
@Composable
fun OrderColumnCreatedErrorAddressPreview() {
  ExchTheme { Surface { OrderColumn(OrderRepositoryMock.orders[2]) } }
}

@Preview("order - cancelled - error address", widthDp = 360)
@Composable
fun OrderColumnCancelledPreview() {
  ExchTheme { Surface { OrderColumn(OrderRepositoryMock.orders[3]) } }
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
