package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderExchanging
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderExchangingErrorRefundAvailable
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.components.Notice
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking

@Composable
fun OrderRefundPending(
    order: Order,
) {
  OrderStateCard {
    Text(
        if (order.stateError == null) stringResource(R.string.order_state_refund_pending_title)
        else stringResource(R.string.order_state_refund_pending_title_error),
        style = MaterialTheme.typography.headlineSmall,
    )

    if (order.fromAmountReceived != null) {
      SelectionContainer {
        Text(
            stringResource(
                R.string.you_have_sent_us_amount, order.fromAmountReceived, order.fromCurrency))
      }
    }

    if (order.transactionIdReceived != null) {
      Column {
        Text(stringResource(R.string.label_transaction_id))
        SelectionContainer {
          TransactionText(fromCurrency = order.fromCurrency, txid = order.transactionIdReceived)
        }
      }
    }

    if (order.stateError == null) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.order_state_refund_pending_desc),
            modifier = Modifier.padding(end = 4.dp))
        CircularProgressIndicator(Modifier.size(18.dp))
      }
    } else {
      Spacer(Modifier)
    }
  }
}

@Preview
@Composable
fun OrderRefundPendingPreview() {
  ExchTheme {
    Surface {
      OrderRefundPending(
          order = orderExchanging,
      )
    }
  }
}
