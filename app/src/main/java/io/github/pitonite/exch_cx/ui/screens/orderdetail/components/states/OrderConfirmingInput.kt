package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderConfirmingInput
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderConfirmingInputEthNote
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun OrderConfirmingInput(
    order: Order,
) {
  OrderStateCard {
    Text(
        stringResource(R.string.order_state_confirming_input_title),
        style = MaterialTheme.typography.headlineSmall,
    )

    Text(stringResource(R.string.order_state_confirming_input_desc))

    Spacer(Modifier)

    if (order.transactionIdReceived != null) {
      Column {
        Text(stringResource(R.string.label_detected_transaction_id))
        SelectionContainer { TransactionText(currency = order.fromCurrency, txid = order.transactionIdReceived) }
      }
    } else if (order.fromCurrency.lowercase() == "eth") {
      Text(stringResource(R.string.notice_eth_txid_notice))
    }

    if (order.fromAmountReceived != null) {
      SelectionContainer {
        Text(stringResource(R.string.detected_amount, order.fromAmountReceived, order.fromCurrency))
      }
    }

  }
}

@Preview
@Composable
fun OrderConfirmingInputPreview() {
  ExchTheme { Surface { OrderConfirmingInput(orderConfirmingInput) } }
}

@Preview
@Composable
fun OrderConfirmingInputEthNotePreview() {
  ExchTheme { Surface { OrderConfirmingInput(orderConfirmingInputEthNote) } }
}
