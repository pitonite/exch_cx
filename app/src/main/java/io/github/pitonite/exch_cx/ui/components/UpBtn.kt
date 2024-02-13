package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun UpBtn(upPress: () -> Unit) {
  IconButton(
      onClick = upPress,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).size(36.dp)) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.label_back),
            tint = LocalContentColor.current)
      }
}

@Preview("default", apiLevel = 33)
@Composable
fun UpBtnPreview() {
  ExchTheme { UpBtn(upPress = {}) }
}
