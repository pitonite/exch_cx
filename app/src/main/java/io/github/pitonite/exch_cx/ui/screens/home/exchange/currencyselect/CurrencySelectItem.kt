package io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.ui.components.ExchDrawable
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.nonScaledSp
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun CurrencySelectItem(
    modifier: Modifier = Modifier,
    currency: CurrencyDetail,
    colors: ButtonColors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    onClick: () -> Unit = {},
    currencySelection: CurrencySelection,
    enabled: Boolean = true,
) {
  Button(
      onClick = onClick,
      modifier = modifier,
      enabled = enabled,
      shape = MaterialTheme.shapes.small,
      contentPadding = PaddingValues(dimensionResource(R.dimen.padding_sm)),
      colors = colors,
  ) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      ExchDrawable(
          name =
              if (currency.name.isNullOrEmpty()) {
                "generic"
              } else currency.name.lowercase(),
          colorFilter =
              if (!enabled) {
                val matrix = ColorMatrix()
                matrix.setToSaturation(0.4f)
                ColorFilter.colorMatrix(matrix)
              } else null,
      )
      Text(
          modifier = Modifier.padding(start = 5.dp),
          text =
              if (currency.name.isNullOrEmpty()) {
                "UKWN"
              } else currency.name.uppercase(),
          fontSize = 23.sp.nonScaledSp,
      )
      Spacer(Modifier.weight(1f))
      if (currencySelection == CurrencySelection.TO) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
          Text(
              text = stringResource(R.string.available_amount),
              fontSize = 10.sp,
          )
          Text(
              text = currency.reserve.setScale(4, RoundingMode.FLOOR).toString(),
              fontSize = 16.sp,
          )
        }
      }
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun CurrencySelectItemPreview() {
  ExchTheme {
    CurrencySelectItem(
        currency =
            CurrencyDetail(
                "BTC",
                "0.000001231".toBigDecimalOrNull()!!,
            ),
        currencySelection = CurrencySelection.TO,
    )
  }
}
