package io.github.pitonite.exch_cx.ui.screens.alerts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.components.LargeDropdownMenu
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

val comparisonOptions =
    persistentListOf(
        R.string.dialog_alert_is_more_than to 1,
        R.string.dialog_alert_is_equal_to to 0,
        R.string.dialog_alert_is_less_than to -1,
        R.string.dialog_alert_has_changed to null,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonSelectionInput(
    modifier: Modifier = Modifier,
    value: Int?,
    onOptionSelected: (Int?) -> Unit,
    enabled: Boolean = true,
) {
  val context = LocalContext.current
  val selectedIndex = comparisonOptions.indexOfFirst { it.second == value }

  LargeDropdownMenu(
      modifier = modifier,
      items = comparisonOptions.map { context.getString(it.first) }.toPersistentList(),
      selectedIndex = selectedIndex,
      onItemSelected = { index, _ -> onOptionSelected(comparisonOptions[index].second) },
      enabled = enabled,
      trigger = { expanded ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.padding_sm),
                vertical = dimensionResource(R.dimen.padding_md),
            ),
        ) {
          Text(
              stringResource(comparisonOptions[selectedIndex].first),
              Modifier.width(IntrinsicSize.Max),
          )
          val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
          Icon(icon, "")
        }
      })
}

@Preview("input")
@Composable
fun ComparisonSelectionInputPreview() {
  ExchTheme {
    Surface {
      ComparisonSelectionInput(
          value = null,
          onOptionSelected = {},
      )
    }
  }
}
