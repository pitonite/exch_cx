package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun Card(
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  errorColor: Color =  MaterialTheme.colorScheme.errorContainer,
  content: @Composable () -> Unit,
) {
  Surface(
      color =
          if (isError) errorColor
          else MaterialTheme.colorScheme.surfaceContainerHigh,
      shape = CardDefaults.shape,
      modifier = modifier.fillMaxWidth(),
      content = content)
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isError: Boolean = false,
    errorColor: Color =  MaterialTheme.colorScheme.errorContainer,
    content: @Composable () -> Unit,
) {
  Surface(
      color =
          if (isError) errorColor
          else MaterialTheme.colorScheme.surfaceContainerHigh,
      shape = CardDefaults.shape,
      modifier = modifier.fillMaxWidth(),
      onClick = onClick,
      content = content)
}

@Preview("default")
@Composable
fun CardPreview() {
  ExchTheme {
    Card {
      Column(Modifier.padding(50.dp)) {
        //
      }
    }
  }
}

@Preview("error")
@Composable
fun CardErrorPreview() {
  ExchTheme { Card(isError = true) { Column(Modifier.padding(50.dp)) {} } }
}
