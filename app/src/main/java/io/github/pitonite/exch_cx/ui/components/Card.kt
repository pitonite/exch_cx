package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
  Surface(
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
      shape = CardDefaults.shape,
      modifier = modifier.fillMaxWidth(),
      content = content)
}

@Composable
fun Card(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  Surface(
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
      shape = CardDefaults.shape,
      modifier = modifier.fillMaxWidth(),
      onClick = onClick,
      content = content)
}
