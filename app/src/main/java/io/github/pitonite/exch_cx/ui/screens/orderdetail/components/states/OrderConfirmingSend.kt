package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderConfirmingSend
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.components.TextWithLoading
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun OrderConfirmingSend(
    order: Order,
) {
  OrderStateCard {
    Text(
        stringResource(R.string.order_state_confirming_send_title),
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

    Spacer(Modifier)

    if (order.toAmount != null) {
      Text(stringResource(R.string.you_will_receive, order.toAmount, order.toCurrency))
    }

    if (!order.transactionIdSent.isNullOrBlank()) {
      Column {
        Text(stringResource(R.string.label_transaction_id))
        SelectionContainer {
          TransactionText(currency = order.toCurrency, txid = order.transactionIdSent)
        }
      }
    }

    Spacer(Modifier)

    if (order.stateError == null) {
      TextWithLoading(stringResource(R.string.order_state_confirming_send_desc))
    } else {
      Text(stringResource(R.string.order_state_unexpected_error))
    }
  }
}

@Preview
@Composable
fun OrderConfirmingSendPreview() {
  ExchTheme {
    Surface {
      OrderConfirmingSend(
          order = orderConfirmingSend,
      )
    }
  }
}
