package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import java.math.BigDecimal

@Composable
fun CurrencyInput(
    modifier: Modifier = Modifier,
    value: String = "",
    placeholder: String = "0.00",
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit = {},
    currency: String,
    minValue: BigDecimal? = null,
    maxValue: BigDecimal? = BigDecimal.valueOf(10000),
    onNavigateToRoute: (route: String) -> Unit,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    currencySelection: CurrencySelection,
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
    CurrencyPicker(
        currency = currency,
        onNavigateToRoute = onNavigateToRoute,
        enabled = enabled,
        currencySelection = currencySelection,
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
    )
  }
}
