package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.ui.navigation.SecondaryDestinations
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import java.math.BigDecimal
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CurrencyInput(
    modifier: Modifier = Modifier,
    value: String = "",
    placeholder: String = "0.00",
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit = {},
    currency: String,
    minValue: BigDecimal? = null,
    maxValue: BigDecimal? = null,
    onNavigateToRoute: (route: String) -> Unit,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    currencySelection: CurrencySelection,
    currencyList: PersistentList<CurrencyDetail>,
    onCurrencySelected: (CurrencyDetail) -> Unit,
    showReserveAlertTip: Boolean = false,
    onReserveAlertTipDismissed: () -> Unit = {},
) {
  Row(
      modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DecimalInputField(
        value = value,
        placeholder = placeholder,
        onValueChange = onValueChange,
        onFocusLost = onFocusLost,
        minValue = minValue,
        maxValue = maxValue,
        modifier = Modifier.weight(1f).padding(horizontal = dimensionResource(R.dimen.padding_md)),
        textStyle =
            LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontSize = 24.sp,
                color =
                    if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            ),
        enabled = enabled,
        imeAction = imeAction,
    )
    val showSelectDialog = remember { mutableStateOf(false) }
    CurrencyPicker(
        currencyList = currencyList,
        currency = currency,
        enabled = enabled,
        title = {
          Text(
              if (currencySelection == CurrencySelection.FROM) stringResource(R.string.you_pay)
              else stringResource(R.string.you_receive))
        },
        actions = {
          if (currencySelection == CurrencySelection.TO) {
            IconButton(
                onClick = {
                  showSelectDialog.value = false
                  onNavigateToRoute(SecondaryDestinations.ALERTS_ROUTE)
                },
            ) {
              Icon(
                  modifier = Modifier.size(32.dp),
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = stringResource(R.string.alerts),
              )
            }
          }
        },
        showReserveAlertTip = showReserveAlertTip,
        onReserveAlertTipDismissed = onReserveAlertTipDismissed,
        showReserves = currencySelection == CurrencySelection.TO,
        onCurrencySelected = onCurrencySelected,
        showSelectDialog = showSelectDialog,
    )
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Preview("rtl", locale = "ar")
@Composable
fun CurrencyInputPreview() {
  ExchTheme(darkTheme = true) {
    CurrencyInput(
        currency = "btc",
        onNavigateToRoute = {},
        onValueChange = {},
        currencySelection = CurrencySelection.FROM,
        currencyList = persistentListOf(),
        onCurrencySelected = {},
    )
  }
}
