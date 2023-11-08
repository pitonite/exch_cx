package io.github.pitonite.exch_cx.ui.screens.home.exchange

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock
import io.github.pitonite.exch_cx.data.RateFeeRepositoryMock
import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.model.api.NetworkFeeOption
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.CurrencyInput
import io.github.pitonite.exch_cx.ui.components.RefreshButton
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.components.Tip
import io.github.pitonite.exch_cx.ui.navigation.SecondaryDestinations
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.noRippleClickable
import io.github.pitonite.exch_cx.utils.nonScaledSp
import java.math.BigDecimal

val FeeStringResourceMap =
    mutableMapOf<NetworkFeeOption, Int>(
        NetworkFeeOption.QUICK to R.string.QUICK,
        NetworkFeeOption.MEDIUM to R.string.MEDIUM,
        NetworkFeeOption.SLOW to R.string.SLOW,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Exchange(
    modifier: Modifier = Modifier,
    viewModel: ExchangeViewModel,
    onOrderSelected: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
  val focusManager = LocalFocusManager.current

  Scaffold(
      snackbarHost = { SnackbarHost(hostState = SnackbarManager.snackbarHostState) },
      topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(),
            title = { Text(stringResource(R.string.exchange)) },
            navigationIcon = {
              IconButton(onClick = { onNavigateToRoute(SecondaryDestinations.SETTINGS_ROUTE) }) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                )
              }
            },
            actions = {
              RefreshButton(
                  onClick = { viewModel.updateFeeRates() },
                  enabled = !uiState.refreshing,
                  refreshing = uiState.refreshing,
              )
            },
        )
      }) { padding ->
        Column(
            modifier =
                modifier
                    .padding(padding)
                    .padding(horizontal = dimensionResource(R.dimen.page_padding))
                    .verticalScroll(rememberScrollState())
                    .noRippleClickable() { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
        ) {
          AnimatedVisibility(visible = !userSettings.isExchangeTipDismissed) {
            Tip(stringResource(R.string.tip_exchange)) { viewModel.setIsExchangeTipDismissed(true) }
          }

          // Conversion Card
          Card {
            Column(
                modifier =
                    Modifier.padding(horizontal = dimensionResource(R.dimen.padding_md))
                        .padding(
                            vertical = dimensionResource(R.dimen.padding_xl),
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
            ) {
              Column {
                Text(
                    stringResource(R.string.you_pay),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_md)),
                )
                CurrencyInput(
                    value = viewModel.fromAmount,
                    currency = uiState.fromCurrency,
                    onNavigateToRoute = onNavigateToRoute,
                    onValueChange = viewModel::updateFromAmount,
                    onFocusLost = { viewModel.updateConversionAmounts(CurrencySelection.FROM) },
                    enabled = uiState.enabled,
                    currencySelection = CurrencySelection.FROM,
                )
              }

              Row(
                  modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_sm)),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                HorizontalDivider(Modifier.weight(1f))
                OutlinedIconButton(
                    onClick = { viewModel.swapCurrencies() },
                    border =
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    enabled = uiState.enabled,
                ) {
                  Icon(
                      imageVector = Icons.Outlined.SwapVert,
                      contentDescription = stringResource(R.string.label_swap_currencies),
                      tint = LocalContentColor.current)
                }
              }

              Column {
                Text(
                    stringResource(R.string.you_receive),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_md)),
                )
                CurrencyInput(
                    value = viewModel.toAmount,
                    currency = uiState.toCurrency,
                    onNavigateToRoute = onNavigateToRoute,
                    onValueChange = viewModel::updateToAmount,
                    onFocusLost = { viewModel.updateConversionAmounts(CurrencySelection.TO) },
                    enabled = uiState.enabled,
                    imeAction = ImeAction.Done,
                    currencySelection = CurrencySelection.TO,
                )
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_lg))) {
                      SegmentedButton(
                          label = {
                            Text(
                                stringResource(R.string.FLAT),
                                fontSize = 20.sp.nonScaledSp,
                            )
                          },
                          onClick = { viewModel.updateFeeRateMode(RateFeeMode.FLAT) },
                          selected = uiState.rateFeeMode == RateFeeMode.FLAT,
                          shape =
                              SegmentedButtonDefaults.itemShape(
                                  index = 0,
                                  count = 2,
                                  baseShape =
                                      RoundedCornerShape(
                                          dimensionResource(R.dimen.rounded_sm),
                                      )),
                          enabled = uiState.enabled,
                      )
                      SegmentedButton(
                          label = {
                            Text(
                                stringResource(R.string.DYNAMIC),
                                fontSize = 20.sp.nonScaledSp,
                            )
                          },
                          onClick = { viewModel.updateFeeRateMode(RateFeeMode.DYNAMIC) },
                          selected = uiState.rateFeeMode == RateFeeMode.DYNAMIC,
                          shape =
                              SegmentedButtonDefaults.itemShape(
                                  index = 1,
                                  count = 2,
                                  baseShape =
                                      RoundedCornerShape(dimensionResource(R.dimen.rounded_sm)),
                              ),
                          enabled = uiState.enabled,
                      )
                    }
                if (uiState.enabled && uiState.rateFee != null) {
                  Text(
                      stringResource(R.string.label_service_fee) +
                          " ${uiState.rateFee!!.svcFee.setScale(1)}%",
                      fontSize = 15.sp)
                }
              }
            }
          }

          // network fee
          if (uiState.rateFee?.networkFee != null) {
            Card {
              Column(
                  modifier =
                      Modifier.padding(horizontal = dimensionResource(R.dimen.padding_sm))
                          .padding(
                              vertical = dimensionResource(R.dimen.padding_lg),
                          ),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
              ) {
                Text(
                    stringResource(R.string.label_network_fee),
                    fontSize = 20.sp,
                )

                SingleChoiceSegmentedButtonRow {
                  val keys = uiState.rateFee!!.networkFee!!.keys
                  keys.forEachIndexed { i, el ->
                    SegmentedButton(
                        label = {
                          Text(
                              stringResource(
                                  FeeStringResourceMap.getOrDefault(el, R.string.Unknown)),
                              fontSize = 20.sp.nonScaledSp,
                          )
                        },
                        onClick = { viewModel.updateNetworkFeeOption(el) },
                        selected = uiState.networkFeeOption == el,
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index = i,
                                count = keys.size,
                                baseShape =
                                    RoundedCornerShape(
                                        dimensionResource(R.dimen.rounded_sm),
                                    )),
                        enabled = uiState.enabled,
                    )
                  }
                }

                val chosenAmount =
                    uiState.rateFee!!
                        .networkFee!!
                        .getOrDefault(uiState.networkFeeOption, BigDecimal.ZERO)
                Text(
                    stringResource(R.string.label_amount) +
                        " " +
                        (if (chosenAmount > BigDecimal.ZERO) chosenAmount
                        else stringResource(R.string.Free)),
                    fontSize = 15.sp,
                )
              }
            }
          }

          // start of address card
          Card {
            Column(
                modifier =
                    Modifier.padding(horizontal = dimensionResource(R.dimen.padding_xl))
                        .padding(
                            vertical = dimensionResource(R.dimen.padding_xl),
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xl)),
            ) {
              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth(),
                  value = viewModel.toAddress,
                  label = { Text(stringResource(R.string.label_to_address)) },
                  onValueChange = viewModel::updateToAddress,
                  supportingText = { Text(stringResource(R.string.hint_to_address_input)) })

              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth(),
                  value = viewModel.refundAddress,
                  label = { Text(stringResource(R.string.label_refund_address)) },
                  onValueChange = viewModel::updateRefundAddress,
                  supportingText = {
                    val importHintText = buildAnnotatedString {
                      append(stringResource(R.string.hint_refund_address_input_p1))
                      append(" ")
                      val p2 = stringResource(R.string.hint_refund_address_input_p2)
                      withStyle(
                          style =
                              SpanStyle(
                                  textDecoration = TextDecoration.Underline,
                                  fontWeight = FontWeight.Bold)) {
                            append(p2)
                          }
                    }
                    Text(importHintText)
                  })
            }
          }
          // end of address card

          Button(
              onClick = {
                viewModel.createOrder(
                    onOrderCreated = onOrderSelected,
                )
              },
              enabled = uiState.enabled) {
                Text(stringResource(R.string.label_create_order), fontSize = 18.sp)
              }

          Spacer(Modifier)
        }
      }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun ExchangePreview() {
  val viewModel =
      ExchangeViewModel(
          SavedStateHandle(),
          RateFeeRepositoryMock(),
          UserSettingsRepositoryMock(),
          OrderRepositoryMock(),
      )
  viewModel.updateWorking(false)
  ExchTheme(darkTheme = true) {
    Exchange(
        viewModel = viewModel,
        onNavigateToRoute = {},
        onOrderSelected = {},
    )
  }
}
