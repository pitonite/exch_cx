package io.github.pitonite.exch_cx.ui.screens.home.orders

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orders
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.codified.enums.toLocalizedString

@Composable
fun OrderItem(order: Order, modifier: Modifier = Modifier, onClick: () -> Unit) {
  Card(modifier, onClick) {
    Column(
        modifier =
            Modifier.padding(
                vertical = dimensionResource(R.dimen.padding_md),
                horizontal = dimensionResource(R.dimen.padding_lg)),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
      ) {
        Text(order.id)
        Text(
            text =
                DateUtils.getRelativeTimeSpanString(
                        order.createdAt.time,
                        System.currentTimeMillis(),
                        0L,
                        DateUtils.FORMAT_ABBREV_ALL)
                    .toString(),
            fontSize = 13.sp,
        )
      }

      HorizontalDivider()
      Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
      ExchangePairRow(fromCurrency = order.fromCurrency, toCurrency = order.toCurrency)

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.label_status))

        val statusText =
            if (order.stateError != null) order.stateError.toLocalizedString()
            else order.state.toLocalizedString()
        Text(
            statusText,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_md)),
            color =
                if (order.stateError != null) MaterialTheme.colorScheme.error
                else Color.Unspecified,
        )
      }
    }
  }
}

@Preview("default")
@Preview("arabic", locale = "fr")
@Composable
fun OrderItemPreview() {
  ExchTheme(darkTheme = true) {
    OrderItem(
        orders[0],
        onClick = {},
    )
  }
}
