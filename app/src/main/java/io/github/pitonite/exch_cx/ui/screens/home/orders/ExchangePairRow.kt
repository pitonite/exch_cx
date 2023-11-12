package io.github.pitonite.exch_cx.ui.screens.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.components.ExchDrawable

@Composable
fun ExchangePairRow(modifier: Modifier = Modifier, fromCurrency: String, toCurrency: String) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    ExchDrawable(
        name = fromCurrency.lowercase(),
    )
    Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
    Text(fromCurrency.uppercase(), Modifier.padding(start = dimensionResource(R.dimen.padding_md)))

    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = stringResource(R.string.label_to),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_md)))

    ExchDrawable(
        name = toCurrency.lowercase(),
    )
    Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
    Text(toCurrency.uppercase(), Modifier.padding(start = dimensionResource(R.dimen.padding_md)))
  }
}

@Preview(showBackground = true, widthDp = 300, locale = "en")
@Composable
fun ExchangePairRowPreview() {
  ExchangePairRow(fromCurrency = "BTC", toCurrency = "ETH")
}
