package io.github.pitonite.exch_cx.ui.screens.orderdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.letterOfGuaranteeExample

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterOfGuaranteeDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    letterOfGuarantee: String,
    onDismissRequest: () -> Unit = {},
) {
  if (show) {
    AlertDialog(
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
              text = stringResource(R.string.title_letter_of_guarantee),
              style = MaterialTheme.typography.titleMedium,
          )

          OutlinedTextField(
              letterOfGuarantee,
              {},
              readOnly = true,
          )

          Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md))) {
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
fun LetterOfGuaranteeDialogPreview() {
  LetterOfGuaranteeDialog(
      show = true, onDismissRequest = {}, letterOfGuarantee = letterOfGuaranteeExample)
}
