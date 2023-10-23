package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.navigation.ExchangeSections
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelection
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.nonScaledSp

@Composable
fun CurrencyPicker(
    modifier: Modifier = Modifier,
    currency: String? = null,
    enabled: Boolean = true,
    onNavigateToRoute: (String) -> Unit,
    currencySelection: CurrencySelection = CurrencySelection.FROM,
) {

  Button(
      onClick = {
        onNavigateToRoute(
            "${ExchangeSections.CURRENCY_SELECT.route}?${NavArgs.SELECTION_KEY}=${currencySelection.name}")
      },
      modifier = modifier,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onSurface,
              disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          ),
      enabled = enabled,
      shape = MaterialTheme.shapes.extraSmall,
      contentPadding = PaddingValues(start = dimensionResource(R.dimen.padding_sm), top = dimensionResource(R.dimen.padding_sm), bottom = dimensionResource(R.dimen.padding_sm)),
  ) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      ExchDrawable(
          name =
              if (currency.isNullOrEmpty()) {
                "generic"
              } else currency.lowercase(),
          colorFilter =
              if (!enabled) {
                val matrix = ColorMatrix()
                matrix.setToSaturation(0.4f)
                ColorFilter.colorMatrix(matrix)
              } else null,
      )
      Text(
          modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_sm)),
          text =
              if (currency.isNullOrEmpty()) {
                "UKWN"
              } else currency.uppercase(),
          fontSize = 23.sp.nonScaledSp,
      )
      Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = stringResource(R.string.dropdown_arrow),
          modifier = Modifier.size(30.dp),
      )
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun CurrencyPickerPreview() {
  ExchTheme(darkTheme = true) {
    CurrencyPicker(
        currency = "btc",
        onNavigateToRoute = {},
    )
  }
}
