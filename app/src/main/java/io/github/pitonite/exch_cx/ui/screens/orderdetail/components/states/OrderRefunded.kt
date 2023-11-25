package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderRefunded
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderRefundedWithPrivateKey
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.HiddenContentDialog
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun OrderRefunded(
    order: Order,
) {
  OrderStateCard {
    Text(
        stringResource(R.string.order_state_refunded_title),
        style = MaterialTheme.typography.headlineSmall,
    )

    if (order.fromAmountReceived != null) {
      SelectionContainer {
        Text(
            stringResource(
                R.string.you_have_sent_us_amount, order.fromAmountReceived, order.fromCurrency))
      }
    }

    if (!order.transactionIdReceived.isNullOrBlank()) {
      Column {
        Text(stringResource(R.string.label_transaction_id))
        SelectionContainer {
          TransactionText(currency = order.fromCurrency, txid = order.transactionIdReceived)
        }
      }
    }

    if (order.refundPrivateKey != null) {

      if (order.toAmount != null) {
        Text(
            stringResource(R.string.you_have_been_given_amount, order.toAmount, order.fromCurrency))
      }

      Text(stringResource(R.string.order_state_refunded_private_key_desc))

      var showPrivateKey by remember { mutableStateOf(false) }

      Button(onClick = { showPrivateKey = true }) {
        Text(stringResource(R.string.show_private_key))
      }

      HiddenContentDialog(
          show = showPrivateKey,
          title = stringResource(R.string.title_private_key),
          content = order.refundPrivateKey,
          onDismissRequest = { showPrivateKey = false })
    } else {
      Text(stringResource(R.string.order_state_refunded_desc))
    }
  }
}

@Preview
@Composable
fun OrderRefundedPreview() {
  ExchTheme {
    Surface {
      OrderRefunded(
          order = orderRefunded,
      )
    }
  }
}

@Preview
@Composable
fun OrderRefundedWithPrivateKeyPreview() {
  ExchTheme {
    Surface {
      OrderRefunded(
          order = orderRefundedWithPrivateKey,
      )
    }
  }
}
