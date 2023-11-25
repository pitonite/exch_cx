package io.github.pitonite.exch_cx.ui.screens.orderdetail

import android.Manifest
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderAwaitingInputMaxInputZero
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderCancelled
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderCreated
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderCreatedToAddressInvalid
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderRefunded
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderUnknownState
import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.network.getExchDomain
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.getTranslation
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.CopyableText
import io.github.pitonite.exch_cx.ui.components.RefreshButton
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.ui.screens.home.orders.ExchangePairRow
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.AutomaticOrderUpdateDialog
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.HiddenContentDialog
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderAwaitingInput
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderCancelled
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderComplete
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderConfirmingInput
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderConfirmingRefund
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderConfirmingSend
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderCreated
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderExchanging
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderRefundPending
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderRefundRequest
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states.OrderRefunded
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.codified.enums.toLocalizedString
import io.github.pitonite.exch_cx.utils.createNotificationChannels
import io.github.pitonite.exch_cx.utils.isWorking
import io.github.pitonite.exch_cx.utils.noRippleClickable
import io.github.pitonite.exch_cx.utils.verticalFadingEdge
import java.math.RoundingMode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// TODO: remove if network indicator was provided by the api
val etheriumBasedCoins = "eth|dai|usdt|usdc".toRegex(RegexOption.IGNORE_CASE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetail(
    viewModel: OrderDetailViewModel,
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
    navigateToOrderSupport: (String) -> Unit
) {
  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current
  val order by viewModel.order.collectAsStateWithLifecycle()
  val settings by viewModel.userSettings.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
  val focusManager = LocalFocusManager.current

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
          createNotificationChannels(context)
        }
      }

  if (order?.archived == false) {
    LaunchedEffect(true) {
      // You can replace this with lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) if
      // needed
      coroutineScope {
        while (isActive) {
          viewModel.refreshOrder()
          delay(15000L)
        }
      }
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
                SelectionContainer {
                  Text(
                      viewModel.orderid.value ?: "",
                      color = MaterialTheme.colorScheme.primary,
                      fontSize = 16.sp)
                }
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
                    enabled = !viewModel.refreshWorkState.isWorking(),
                    refreshing = viewModel.refreshWorkState.isWorking(),
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
                      },
                  )

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
                      onClick = { viewModel.toggleArchive() },
                  )

                  DropdownMenuItem(
                      leadingIcon = {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                        )
                      },
                      text = { Text(stringResource(R.string.support_chat)) },
                      onClick = {
                        val orderid = viewModel.orderid.value
                        if (!orderid.isNullOrBlank()) {
                          navigateToOrderSupport(orderid)
                        }
                      },
                  )
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
                .verticalScroll(scrollState)
                .noRippleClickable { focusManager.clearFocus() },
        ) {
          AutomaticOrderUpdateDialog(show = !settings.hasShownOrderBackgroundUpdateNotice) {
            if (it) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            }
            viewModel.onAutomaticDialogResult(it)
          }
          if (order != null) {
            OrderColumn(viewModel, order!!, navigateToOrderSupport)
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
fun OrderColumn(
    viewModel: OrderDetailViewModel,
    order: Order,
    navigateToOrderSupport: (String) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding))) {
    ExchangePairRow(fromCurrency = order.fromCurrency, toCurrency = order.toCurrency)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
        horizontalAlignment = Alignment.CenterHorizontally) {
          SelectionContainer {
            Text(
                "1 ${order.fromCurrency.uppercase()} = ${order.rate} ${order.toCurrency.uppercase()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
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
        OrderCancelled()
      }
      OrderState.CREATED -> {
        // check if address invalid error is present and show revalidate form
        // else, tell user the address is being generated and user should wait

        OrderCreated(
            order,
            onSubmitNewToAddress = { viewModel.submitNewToAddress(it) },
            submitWorkState = viewModel.submitNewToAddressWorkState)
      }
      OrderState.AWAITING_INPUT -> {
        OrderAwaitingInput(order)
      }
      OrderState.CONFIRMING_INPUT -> {
        // let user know we have detected the funds and are waiting for it to be confirmed
        // currently ETH->[ANY] orders don't show the txid
        OrderConfirmingInput(order)
      }
      OrderState.EXCHANGING -> {
        // the order is enqueued to send the payout.
        // If there is no balance available (for example the user overpaid by mistake or someone
        // else took their amount meanwhile their input was confirming) then the automatic refund
        // option is offered
        // if the payout is sent successfully, the order's state changes to CONFIRMING_SEND.
        OrderExchanging(
            order = order,
            requestRefund = viewModel::requestRefund,
            requestRefundWorkState = viewModel.requestRefundWorkState,
        )
      }
      OrderState.CONFIRMING_SEND -> {
        // once there is a confirmation, the order's state changes to COMPLETE,
        // which is a final state.
        OrderConfirmingSend(order)
      }
      OrderState.REFUND_REQUEST -> {
        // when the user has requested refund,
        // user is required to select the fee during this state
        // also the refund address in case it wasn't provided during the order creation
        // once they submitted the address and the desired fee, the order will enter the
        // REFUND_PENDING state
        // for USDT/DAI/USDC, the order state will directly become REFUNDED, revealing a deposit's
        // address private key to the user
        OrderRefundRequest(
            order,
            requestRefundConfirm = { viewModel.requestRefundConfirm(it) },
            requestRefundConfirmWorkState = viewModel.requestRefundConfirmWorkState)
      }
      OrderState.REFUND_PENDING -> {
        // the order is enqueued for the refund payout. Once the payout sent, the state will change
        // to CONFIRMING_REFUND
        OrderRefundPending(order)
      }
      OrderState.CONFIRMING_REFUND -> {
        // Once the payout txid is confirmed, the order state will change to REFUNDED, this is a
        // final state
        OrderConfirmingRefund(order)
      }
      OrderState.REFUNDED -> {
        OrderRefunded(order)
      }
      OrderState.COMPLETE -> {
        // user can request delete at this state
        OrderComplete(
            order = order,
            requestOrderDataDelete = { viewModel.requestOrderDataDelete() },
            requestOrderDataDeleteWorkState = viewModel.requestOrderDataDeleteWorkState)
      }
      else -> {
        // this is when order state is null, this can happen if the states are experimental

        OrderStateCard {
          Text(
              stringResource(R.string.order_state_unknown),
              style = MaterialTheme.typography.headlineSmall,
          )

          Text(stringResource(R.string.order_state_unknown_tip))
        }
      }
    }

    OrderStateCard {
      Column {
        Text(stringResource(R.string.label_your_address, order.toCurrency))
        CopyableText(order.toAddress, copyConfirmationMessage = R.string.snack_address_copied)
      }



      if (!order.transactionIdSent.isNullOrBlank()) {
        val isRefund = order.state.code().lowercase().startsWith("refund")
        val currency = if (isRefund) order.fromCurrency else order.toCurrency

        if (order.toAmount != null) {
          val msg =  if (isRefund) {
            R.string.order_refund_amount
          } else {
            R.string.order_sent_amount
          }
          Text(stringResource(msg, order.toAmount, currency))
        }

        Column {
          Text(stringResource(R.string.label_transaction_id))
          SelectionContainer {
            TransactionText(currency = currency, txid = order.transactionIdSent)
          }
        }
      }

      if (!order.refundAddress.isNullOrBlank()) {
        Column {
          Text(stringResource(R.string.label_your_refund_address, order.fromCurrency))
          CopyableText(order.refundAddress, copyConfirmationMessage = R.string.snack_address_copied)
        }
      }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg)),
    ) {
      SelectionContainer {
        Text(
            stringResource(
                R.string.order_created_at,
                DateFormat.format("MMM dd, yyyy HH:mm", order.createdAt)),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      val getSupportText = buildAnnotatedString {
        pushStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant))
        append(stringResource(R.string.having_problem_question))
        append(" ")
        pop()
        pushStyle(SpanStyle(color = MaterialTheme.colorScheme.tertiary))
        append(stringResource(R.string.open))
        append(" ")
        pushStringAnnotation("clickable", "clickable")
        append(stringResource(R.string.in_order_support_chat))
      }
      ClickableText(
          getSupportText,
          style = LocalTextStyle.current,
      ) {
        getSupportText.getStringAnnotations(it, it).firstOrNull()?.tag?.let {
          navigateToOrderSupport(order.id)
        }
      }

      if (!order.letterOfGuarantee.isNullOrEmpty()) {
        var showLetter by remember { mutableStateOf(false) }
        val showLetterText = buildAnnotatedString {
          pushStyle(SpanStyle(color = MaterialTheme.colorScheme.tertiary))
          append(stringResource(R.string.show_letter_of_guarantee))
        }
        ClickableText(
            showLetterText,
            style = LocalTextStyle.current,
        ) {
          showLetter = true
        }

        HiddenContentDialog(
            show = showLetter,
            title = stringResource(R.string.title_letter_of_guarantee),
            content = order.letterOfGuarantee,
            onDismissRequest = { showLetter = false })
      }
    }

    Spacer(Modifier)
  }
}

fun getMockViewModel() =
    OrderDetailViewModel(
        SavedStateHandle().apply { this[NavArgs.ORDER_ID_KEY] = "ee902b8a5fe0844d41" },
        OrderRepositoryMock(),
        UserSettingsRepositoryMock(),
    )

@Preview("order - created", widthDp = 360)
@Composable
fun OrderColumnCreatedPreview() {
  ExchTheme { Surface { OrderColumn(getMockViewModel(), orderCreated, {}) } }
}

@Preview("order - created - max zero", widthDp = 360)
@Composable
fun OrderColumnCreatedMaxZeroPreview() {
  ExchTheme { Surface { OrderColumn(getMockViewModel(), orderAwaitingInputMaxInputZero, {}) } }
}

@Preview("order - created - error address", widthDp = 360)
@Composable
fun OrderColumnCreatedErrorAddressPreview() {
  ExchTheme { Surface { OrderColumn(getMockViewModel(), orderCreatedToAddressInvalid, {}) } }
}

@Preview("order - cancelled - error address", widthDp = 360)
@Composable
fun OrderColumnCancelledPreview() {
  ExchTheme { Surface { OrderColumn(getMockViewModel(), orderCancelled, {}) } }
}

@Preview("order - unknown", widthDp = 360)
@Composable
fun OrderColumnUnknownPreview() {
  ExchTheme { Surface { OrderColumn(getMockViewModel(), orderUnknownState, {}) } }
}

@Preview("order - refunded", widthDp = 360)
@Composable
fun OrderColumnRefundedPreview() {
  ExchTheme { Surface { OrderColumn(getMockViewModel(), orderRefunded, {}) } }
}


@Preview("default")
@Composable
fun OrderDetailPreview() {

  ExchTheme {
    OrderDetail(
        viewModel = getMockViewModel(),
        upPress = {},
        navigateToOrderSupport = {},
    )
  }
}
