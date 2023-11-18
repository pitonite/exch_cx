package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderComplete
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderCompleteDeletedInRemote
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.TransactionText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking

@Composable
fun OrderComplete(
    order: Order,
    requestOrderDataDelete: () -> Unit,
    requestOrderDataDeleteWorkState: WorkState,
) {
  OrderStateCard {
    Text(
        stringResource(R.string.order_state_complete_title),
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

    if (order.deletedInRemote) {
      Text(stringResource(R.string.orderstate_complete_data_deleted))
    } else {
      Text(stringResource(R.string.order_state_complete_desc))
      var showDialog by remember { mutableStateOf(false) }

      OrderDataDeleteDialog(
          showDialog,
          onDismiss = { showDialog = false },
          onConfirm = {
            requestOrderDataDelete()
            showDialog = false
          })

      Button(
          onClick = { showDialog = true },
          enabled = !requestOrderDataDeleteWorkState.isWorking(),
          colors =
              ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.error,
                  contentColor = MaterialTheme.colorScheme.onError,
                  disabledContainerColor = MaterialTheme.colorScheme.error,
                  disabledContentColor = MaterialTheme.colorScheme.onError,
              )) {
            if (requestOrderDataDeleteWorkState.isWorking()) {
              CircularProgressIndicator()
            } else {
              Text(stringResource(R.string.delete_data))
            }
          }
    }
  }
}

@Preview
@Composable
fun OrderCompletePreview() {
  ExchTheme {
    Surface {
      OrderComplete(
          order = orderComplete,
          {},
          WorkState.NotWorking,
      )
    }
  }
}

@Preview
@Composable
fun OrderCompleteRemoteDataDeletedPreview() {
  ExchTheme {
    Surface {
      OrderComplete(
          order = orderCompleteDeletedInRemote,
          {},
          WorkState.NotWorking,
      )
    }
  }
}

@Composable
fun OrderDataDeleteDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
  if (show) {
    AlertDialog(
        icon = {
          Icon(
              imageVector = Icons.Default.WarningAmber,
              contentDescription = stringResource(R.string.warning))
        },
        title = { Text(text = stringResource(R.string.warning)) },
        text = {
          Text(text = stringResource(R.string.order_state_complete_delete_data_warning_dialog))
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
          TextButton(
              colors =
                  ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
              onClick = { onConfirm() },
          ) {
            Text(stringResource(R.string.confirm))
          }
        },
        dismissButton = {
          TextButton(
              colors = ButtonDefaults.textButtonColors(contentColor = LocalTextStyle.current.color),
              onClick = { onDismiss() },
          ) {
            Text(stringResource(R.string.dismiss))
          }
        })
  }
}

@Preview
@Composable
fun OrderDataDeleteDialogPreview() {
  ExchTheme { OrderDataDeleteDialog(true, {}, {}) }
}
