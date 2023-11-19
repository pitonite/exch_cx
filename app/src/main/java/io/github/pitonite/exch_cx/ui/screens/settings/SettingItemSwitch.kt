package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import io.github.pitonite.exch_cx.R

@Composable
fun SettingItemSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (newValue: Boolean) -> Unit,
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
  ) {
    Text(text, modifier = Modifier.weight(1f))

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent =
            if (checked) {
              {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
              }
            } else {
              null
            })
  }
}
