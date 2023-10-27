package io.github.pitonite.exch_cx.ui.screens.home.orders

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.ExchDrawable
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import java.math.BigDecimal
import java.util.Date

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
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        ExchDrawable(
            name = order.fromCurrency.lowercase(),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
        Text(
            order.fromCurrency.uppercase(),
            Modifier.padding(start = dimensionResource(R.dimen.padding_md)))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(R.string.label_to),
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_md)))

        ExchDrawable(
            name = order.toCurrency.lowercase(),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
        Text(
            order.toCurrency.uppercase(),
            Modifier.padding(start = dimensionResource(R.dimen.padding_md)))
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.label_status))
        Text(
            order.state.name.uppercase(),
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_md)))
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
        Order(
            id = "ee902b8a5fe0844d41",
            createdAt = Date(1698382396808),
            fromCurrency = "BTC",
            toCurrency = "ETH",
            rate = BigDecimal.valueOf(18.867924528301927),
            rateMode = RateFeeMode.DYNAMIC,
            state = OrderState.CREATED,
            svcFee = BigDecimal.valueOf(1),
            toAddress = "foo_address"),
        onClick = {},
    )
  }
}
