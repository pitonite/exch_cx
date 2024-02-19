package io.github.pitonite.exch_cx.ui.screens.alerts.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.ui.components.CurrencyPicker
import io.github.pitonite.exch_cx.ui.components.DecimalInputField
import io.github.pitonite.exch_cx.ui.components.RefreshButton
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking
import io.github.pitonite.exch_cx.utils.noRippleClickable
import java.math.BigDecimal
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertAddEditDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    currencyList: PersistentList<CurrencyDetail> = persistentListOf(),
    refreshWorkState: WorkState = WorkState.NotWorking,
    editingTrigger: CurrencyReserveTrigger? = null,
    onRefresh: () -> Unit,
    onSave: (CurrencyReserveTrigger) -> Unit,
) {
  val editing = editingTrigger != null
  var editingTriggerCopy by remember {
    mutableStateOf(
        editingTrigger
            ?: CurrencyReserveTrigger(
                currency = "btc",
                targetAmount = BigDecimal.ZERO,
                comparison = 1,
            ))
  }

  BasicAlertDialog(
      modifier = Modifier.width(IntrinsicSize.Max),
      properties =
          DialogProperties(
              usePlatformDefaultWidth = false,
          ),
      onDismissRequest = onDismissRequest,
  ) {
    val focusManager = LocalFocusManager.current

    Card() {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(20.dp),
          modifier =
              modifier
                  .padding(dimensionResource(R.dimen.padding_lg))
                  .fillMaxWidth()
                  .noRippleClickable() { focusManager.clearFocus() },
      ) {
        Text(
            text =
                if (editing) stringResource(R.string.label_editing_alert)
                else stringResource(R.string.label_add_alert),
            style = MaterialTheme.typography.titleLarge,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
          ) {
            Text(
                text = stringResource(R.string.dialog_alert_notify_me_when),
            )
            CurrencyPicker(
                modifier =
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(0.1f),
                        shape = MaterialTheme.shapes.extraSmall),
                currencyList = currencyList,
                currency = editingTriggerCopy.currency,
                title = { Text(stringResource(R.string.select_currency)) },
                onCurrencySelected = {
                  editingTriggerCopy = editingTriggerCopy.copy(currency = it.name)
                },
                textSize = LocalTextStyle.current.fontSize,
                actions = {
                  if (currencyList.isEmpty()) {
                    RefreshButton(
                        onClick = { onRefresh() },
                        enabled = !refreshWorkState.isWorking(),
                        refreshing = refreshWorkState.isWorking(),
                    )
                  }
                },
                showReserves = true,
            )
            Text(
                text = stringResource(R.string.dialog_alert_reserve_word),
            )
          }

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
          ) {
            ComparisonSelectionInput(
                modifier =
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(0.1f),
                        shape = MaterialTheme.shapes.extraSmall),
                value = editingTriggerCopy.comparison,
                onOptionSelected = {
                  editingTriggerCopy = editingTriggerCopy.copy(comparison = it)
                },
            )
            if (editingTriggerCopy.comparison != null) {
              Row(
                  modifier =
                      Modifier.border(
                              1.dp,
                              MaterialTheme.colorScheme.onSurfaceVariant.copy(0.1f),
                              shape = MaterialTheme.shapes.extraSmall)
                          .padding(dimensionResource(R.dimen.padding_md)),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                DecimalInputField(
                    value = editingTriggerCopy.targetAmount?.toString() ?: "",
                    placeholder = "0.00",
                    onValueChange = {
                      editingTriggerCopy =
                          editingTriggerCopy.copy(targetAmount = it.toBigDecimalOrNull())
                    },
                    minValue = BigDecimal.ZERO,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Done,
                )
              }
            }
          }

          val toggleDisableAfterTrigger = {
            editingTriggerCopy = editingTriggerCopy.copy(onlyOnce = !editingTriggerCopy.onlyOnce)
          }

          Row(
              horizontalArrangement = Arrangement.Center,
              modifier =
                  Modifier.selectable(
                          selected = editingTriggerCopy.onlyOnce,
                          onClick = toggleDisableAfterTrigger)
                      .fillMaxWidth()
                      .padding(dimensionResource(R.dimen.padding_md)),
              verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = editingTriggerCopy.onlyOnce, onCheckedChange = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.dialog_alert_only_once))
              }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md))) {
          OutlinedButton(onClick = { onDismissRequest() }) {
            Text(stringResource(R.string.label_cancel))
          }

          Button(onClick = { onSave(editingTriggerCopy) }) {
            if (refreshWorkState.isWorking()) {
              CircularProgressIndicator()
            } else {
              Text(stringResource(R.string.label_save))
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AlertAddEditDialogPreview() {
  AlertAddEditDialog(
      onDismissRequest = {},
      refreshWorkState = WorkState.NotWorking,
      onRefresh = {},
      onSave = {},
  )
}
