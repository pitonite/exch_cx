package io.github.pitonite.exch_cx.ui.screens.orderdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomaticOrderUpdateDialog(
    show: Boolean,
    modifier: Modifier = Modifier,
    onResult: (agreed: Boolean) -> Unit,
) {
  if (show) {
    AlertDialog(
        onDismissRequest = {},
    ) {
      Card() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.padding(dimensionResource(R.dimen.padding_lg)),
        ) {
          Text(
              text = stringResource(R.string.title_order_tracking),
              style = MaterialTheme.typography.titleMedium,
          )
          Text(
              stringResource(R.string.dialog_order_tracking),
              modifier = Modifier.padding(dimensionResource(R.dimen.padding_sm)))

          Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md))) {
            TextButton(onClick = { onResult(false) }) { Text(stringResource(R.string.no), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Light) }
            TextButton(onClick = { onResult(true) }) { Text(stringResource(R.string.yes), fontWeight = FontWeight.Bold) }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 300, locale = "en")
@Composable
fun ImportOrderDialogPreview() {
  AutomaticOrderUpdateDialog(
      show = true,
      onResult = {},
  )
}
