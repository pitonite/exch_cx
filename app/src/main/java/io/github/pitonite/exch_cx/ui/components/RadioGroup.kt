package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf

@Composable
fun RadioGroup(
    options: ImmutableList<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) =
    RadioGroup(
        options = options,
        selectedOption = selectedOption,
        onOptionSelected = onOptionSelected,
        label = { Text(it) },
    )

@Composable
fun <T> RadioGroup(
  options: ImmutableList<T>,
  selectedOption: T,
  onOptionSelected: (T) -> Unit,
  label: @Composable (T) -> Unit,
) {
  Column(
      verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    options.forEach { option ->
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .selectable(
                      selected = (option == selectedOption),
                      onClick = { onOptionSelected(option) }),
          verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = option == selectedOption, onClick = { onOptionSelected(option) })
            Spacer(modifier = Modifier.width(6.dp))
            label(option)
          }
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun RadioGroupPreview() {
  ExchTheme {
    RadioGroup(
        options = persistentListOf("option 1", "option 2"),
        selectedOption = "option 1",
        onOptionSelected = {},
    )
  }
}
