package io.github.pitonite.exch_cx.ui.screens.orderdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun OrderStateCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {

  Card(modifier) {
    Column(
        modifier =
            Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                .padding(
                    vertical = dimensionResource(R.dimen.padding_lg),
                ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_lg)),
    ) {
      content()
    }
  }
}

@Preview
@Composable
fun OrderStateCardPreview() {
  ExchTheme {
    Surface() {
      Column(Modifier.padding(10.dp)) {
        OrderStateCard {
          Text(
              stringResource(R.string.order_state_waiting_for_input_title, "BTC"),
              style = MaterialTheme.typography.headlineSmall)

          Text("foo")
          Text("bar")
        }
      }
    }
  }
}
