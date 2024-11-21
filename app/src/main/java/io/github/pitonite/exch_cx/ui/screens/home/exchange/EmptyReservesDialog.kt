package io.github.pitonite.exch_cx.ui.screens.home.exchange

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import io.github.pitonite.exch_cx.ui.screens.home.orders.getImportError
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking
import io.github.pitonite.exch_cx.utils.rememberQrCodeScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyReservesDialog(
  modifier: Modifier = Modifier,
  show: Boolean,
  onProceedPressed: () -> Unit,
  onDismissRequest: () -> Unit,
  currency: String,
) {
  if (show) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
      Card() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.padding(dimensionResource(R.dimen.padding_lg)),
        ) {
          Text(
              text = stringResource(R.string.notice),
              style = MaterialTheme.typography.titleMedium,
          )
          Text(
              stringResource(R.string.desc_empty_reserve_dialog, currency),
              modifier = Modifier.padding(dimensionResource(R.dimen.padding_sm)),
          )

          Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md))) {
            Button(onClick = { onProceedPressed() }) {
              Text(stringResource(R.string.label_proceed))
            }
            OutlinedButton(onClick = { onDismissRequest() }) {
              Text(stringResource(R.string.label_cancel))
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 300, locale = "en")
@Composable
fun EmptyReservesDialogPreview() {
  EmptyReservesDialog(
      show = true,
      onProceedPressed = {},
      onDismissRequest = {},
      currency = "XMR"
  )
}
