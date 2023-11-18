package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
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
fun OrderExchanging(
    order: Order,
    requestRefund: () -> Unit,
    requestRefundWorkState: WorkState,
) {
  OrderStateCard {
    Text(
        if (order.stateError == null) stringResource(R.string.order_state_exchanging_title)
        else stringResource(R.string.order_state_exchanging_title_error),
        style = MaterialTheme.typography.headlineSmall,
    )

    if (order.fromAmountReceived != null) {
      SelectionContainer {
        Text(
            stringResource(
                R.string.from_amount_received, order.fromAmountReceived, order.fromCurrency))
      }
    }

    if (order.transactionIdReceived != null) {
      Column {
        Text(stringResource(R.string.label_transaction_id))
        SelectionContainer {
          TransactionText(fromCurrency = order.fromCurrency, txid = order.transactionIdReceived)
        }
      }
    } else if (order.fromCurrency.lowercase() == "eth") {
      Text(stringResource(R.string.notice_eth_txid_notice))
    }

    if (order.toAmount != null) {
      Text(stringResource(R.string.you_will_receive, order.toAmount, order.toCurrency))
    }

    if (order.stateError == null) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.order_state_exchanging_working),
            modifier = Modifier.padding(end = 4.dp))
        CircularProgressIndicator(Modifier.size(18.dp))
      }
    } else {
      Spacer(Modifier)
    }

    if (order.refundAvailable) {
      Text(stringResource(R.string.order_state_exchanging_refund_available))
      Button(
          onClick = requestRefund,
          enabled = !requestRefundWorkState.isWorking(),
      ) {
        if (requestRefundWorkState.isWorking()) {
          CircularProgressIndicator()
        } else {
          Text(stringResource(R.string.request_refund))
        }
      }
      if (order.refundAddress == null) {
        Notice(
            stringResource(R.string.order_state_exchanging_refund_available_tip),
            fontSize = 16.sp,
        )
      }
    }
  }
}

@Preview
@Composable
fun OrderExchangingPreview() {
  ExchTheme {
    Surface {
      OrderExchanging(
          order = orderExchanging,
          requestRefund = {},
          requestRefundWorkState = WorkState.NotWorking,
      )
    }
  }
}

@Preview
@Composable
fun OrderExchangingErrorRefundAvailablePreview() {
  ExchTheme {
    Surface {
      OrderExchanging(
          order = orderExchangingErrorRefundAvailable,
          requestRefund = {},
          requestRefundWorkState = WorkState.NotWorking,
      )
    }
  }
}
