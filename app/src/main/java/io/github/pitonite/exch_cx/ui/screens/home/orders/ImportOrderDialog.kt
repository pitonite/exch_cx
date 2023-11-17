package io.github.pitonite.exch_cx.ui.screens.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking

@Composable
fun getImportError(workState: WorkState.Error): String {
  val message = workState.error.message
  if (message == null) {
    return stringResource(R.string.unknown_error)
  } else if (message.contains("Unable to resolve host", true)) {
    return stringResource(R.string.error_unable_to_resolve_host)
  } else if (message.contains("timeout", true)) {
    return stringResource(R.string.error_network_timedout)
  } else if (message.contains("network", true) || message.contains("connect", true)) {
    return stringResource(R.string.error_check_connection)
  } else {
    return stringResource(R.string.error_order_not_found)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportOrderDialog(
    show: Boolean,
    onImportPressed: (String) -> Unit,
    onDismissRequest: () -> Unit = {},
    workState: WorkState = WorkState.NotWorking,
    modifier: Modifier = Modifier,
) {
  if (show) {
    var orderid by remember { mutableStateOf("") }
    val enabled = !workState.isWorking()
    AlertDialog(
        onDismissRequest,
    ) {
      Card() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.padding(dimensionResource(R.dimen.padding_lg)),
        ) {
          Text(
              text = stringResource(R.string.label_import_order),
              style = MaterialTheme.typography.titleMedium,
          )
          Text(
              stringResource(R.string.desc_import_order),
              modifier = Modifier.padding(dimensionResource(R.dimen.padding_sm)))
          OutlinedTextField(
              value = orderid,
              onValueChange = { orderid = it },
              label = { Text(stringResource(R.string.label_order_id)) },
              enabled = enabled,
              isError = workState is WorkState.Error,
              supportingText =
                  if (workState is WorkState.Error) {
                    { Text(getImportError(workState), color = MaterialTheme.colorScheme.error) }
                  } else null,
          )

          Button(onClick = { onImportPressed(orderid) }, enabled = enabled) {
            if (workState.isWorking()) {
              CircularProgressIndicator()
            } else {
              Text(stringResource(R.string.label_import))
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 300, locale = "en")
@Composable
fun ImportOrderDialogPreview() {
  ImportOrderDialog(
      show = true,
      onImportPressed = {},
      onDismissRequest = {},
      workState = WorkState.Error(Error("This is a very long error message to see in preview")))
}
