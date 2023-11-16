package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun OrderCancelled() {
  OrderStateCard() {
    Text(
        modifier=  Modifier.fillMaxWidth(),
        text = stringResource(R.string.error_order_cancelled),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.headlineSmall)
  }
}

@Preview
@Composable
fun OrderCancelledPreview() {
  ExchTheme {
    Surface() {
      OrderCancelled()
    }
  }
}
