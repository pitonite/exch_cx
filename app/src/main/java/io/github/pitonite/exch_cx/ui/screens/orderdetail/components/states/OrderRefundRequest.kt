package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderExchanging
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.components.Notice
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking

@Composable
fun OrderRefundRequest(
    order: Order,
    requestRefundConfirm: (String) -> Unit,
    requestRefundConfirmWorkState: WorkState,
) {
  val focusManager = LocalFocusManager.current

  OrderStateCard {
    Text(
        stringResource(R.string.order_state_refund_request_title),
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
          TransactionText(currency = order.fromCurrency, txid = order.transactionIdReceived)
        }
      }
    }

    Text(stringResource(R.string.order_state_refund_request_desc))

    var refundAddress by rememberSaveable(key = "refund_address_${order.id}") { mutableStateOf("") }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = refundAddress,
        onValueChange = { refundAddress = it },
        label = { Text(stringResource(R.string.label_refund_address)) },
        keyboardActions =
            KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
    )

    var showDialog by remember { mutableStateOf(false) }

    RefundWarningDialog(
        showDialog,
        onDismiss = { showDialog = false },
        onConfirm = {
          requestRefundConfirm(refundAddress)
          showDialog = false
        })

    Button(
        onClick = { showDialog = true },
        enabled = !requestRefundConfirmWorkState.isWorking(),
    ) {
      if (requestRefundConfirmWorkState.isWorking()) {
        CircularProgressIndicator()
      } else {
        Text(stringResource(R.string.confirm))
      }
    }

    Notice(stringResource(R.string.notice_refund_network_fee), fontSize = 15.sp)
  }
}

@Preview
@Composable
fun OrderRefundRequestPreview() {
  ExchTheme {
    Surface {
      OrderRefundRequest(
          order = orderExchanging,
          requestRefundConfirm = {},
          requestRefundConfirmWorkState = WorkState.NotWorking,
      )
    }
  }
}

@Composable
fun RefundWarningDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
  if (show) {
    AlertDialog(
        icon = {
          Icon(
              imageVector = Icons.Default.WarningAmber,
              contentDescription = stringResource(R.string.warning))
        },
        title = { Text(text = stringResource(R.string.warning)) },
        text = { Text(text = stringResource(R.string.order_state_refund_request_warning_dialog)) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
          TextButton(colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),onClick = { onConfirm() },) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
          TextButton(colors = ButtonDefaults.textButtonColors(contentColor = LocalTextStyle.current.color), onClick = { onDismiss() },) {
            Text(stringResource(R.string.dismiss))
          }
        })
  }
}

@Preview
@Composable
fun RefundWarningDialogPreview() {
  ExchTheme { RefundWarningDialog(true, {}, {}) }
}
