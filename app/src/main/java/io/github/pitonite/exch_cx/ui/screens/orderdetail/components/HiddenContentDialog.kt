package io.github.pitonite.exch_cx.ui.screens.orderdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.letterOfGuaranteeExample
import io.github.pitonite.exch_cx.utils.copyToClipboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenContentDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    title: String,
    content: String,
    onDismissRequest: () -> Unit = {},
) {
  val context = LocalContext.current
  if (show) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(dimensionResource(R.dimen.page_padding)),
    ) {
      Card {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.padding(dimensionResource(R.dimen.padding_lg)),
        ) {
          Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
          )

          OutlinedTextField(
              content,
              {},
              readOnly = true,
              maxLines = 14,
          )

          Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md))) {
            OutlinedButton(onClick = { copyToClipboard(context, content) }) {
              Text(
                  stringResource(R.string.accessibility_label_copy),
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Light)
            }
            OutlinedButton(onClick = onDismissRequest) {
              Text(
                  stringResource(R.string.close),
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Light)
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun HiddenContentDialogDialogPreview() {
  HiddenContentDialog(
      show = true,
      onDismissRequest = {},
      title = stringResource(R.string.title_letter_of_guarantee),
      content = letterOfGuaranteeExample)
}
