package io.github.pitonite.exch_cx.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.ExchDrawable
import java.math.BigDecimal
import kotlinx.collections.immutable.persistentMapOf

val comparisonOptionsMap =
    persistentMapOf(
        1 to R.string.dialog_alert_is_more_than,
        0 to R.string.dialog_alert_is_equal_to,
        -1 to R.string.dialog_alert_is_less_than,
        null to R.string.dialog_alert_has_changed,
    )

@Composable
fun AlertItem(
    trigger: CurrencyReserveTrigger,
    onToggle: () -> Unit = {},
    onToggleOnlyOnce: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
  val enabled = trigger.isEnabled

  Card(
      modifier = Modifier.height(IntrinsicSize.Min),
      color =
          if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh
          else MaterialTheme.colorScheme.surfaceContainer,
      onClick = onToggle,
  ) {
    Box {
      Box(
          Modifier.fillMaxHeight()
              .width(dimensionResource(R.dimen.padding_md))
              .background(
                  if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
              ),
      )
      Row(
          modifier =
              Modifier.padding(start = dimensionResource(R.dimen.padding_md))
                  .padding(dimensionResource(R.dimen.padding_md)),
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
          Row(
              horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(stringResource(R.string.alert_when))
            ExchDrawable(
                modifier = Modifier.size(23.dp),
                name =
                    if (trigger.currency.isEmpty()) {
                      "generic"
                    } else trigger.currency.lowercase(),
                colorFilter =
                    if (!enabled) {
                      val matrix = ColorMatrix()
                      matrix.setToSaturation(0.4f)
                      ColorFilter.colorMatrix(matrix)
                    } else null,
            )
            Text(trigger.currency.uppercase())
            Text(stringResource(R.string.dialog_alert_reserve_word))
          }

          Row(
              horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
          ) {
            Text(stringResource(comparisonOptionsMap[trigger.comparison]!!))
            if (trigger.comparison != null) {
              Text(
                  trigger.targetAmount?.toString() ?: "",
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.widthIn(Dp.Unspecified, 100.dp),
                  maxLines = 1,
              )
            }
          }
        }
        Spacer(Modifier.weight(1f))

        val iconBtnContainerColor =
            if (enabled) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        Row {
          FilledIconButton(
              onClick = onToggleOnlyOnce,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = iconBtnContainerColor,
                  ),
          ) {
            if (trigger.onlyOnce) {
              Icon(
                  Icons.Outlined.LooksOne,
                  contentDescription = stringResource(R.string.toggle_recurrence))
            } else {
              Icon(
                  Icons.Default.Autorenew,
                  contentDescription = stringResource(R.string.toggle_recurrence))
            }
          }
          FilledIconButton(
              onClick = onEdit,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = iconBtnContainerColor,
                  ),
          ) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
          }
          FilledIconButton(
              onClick = onDelete,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor =
                          if (enabled) MaterialTheme.colorScheme.errorContainer
                          else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                      contentColor = MaterialTheme.colorScheme.error,
                  ),
          ) {
            Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.delete))
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AlertItemPreview() {
  AlertItem(
      trigger =
          CurrencyReserveTrigger(
              comparison = 1,
              targetAmount = BigDecimal.valueOf(11.40214),
              currency = "XMR",
          ),
  )
}

@Preview(showBackground = true)
@Composable
fun AlertItemPreview2() {
  AlertItem(
      trigger =
          CurrencyReserveTrigger(
              comparison = 1,
              targetAmount = BigDecimal.valueOf(11.40214),
              currency = "XMR",
              isEnabled = false,
          ),
  )
}

@Preview(showBackground = true)
@Composable
fun AlertItemPreview3() {
  AlertItem(
      trigger =
          CurrencyReserveTrigger(
              comparison = 1,
              targetAmount = BigDecimal.valueOf(0),
              currency = "XMR",
              onlyOnce = true,
          ),
  )
}
