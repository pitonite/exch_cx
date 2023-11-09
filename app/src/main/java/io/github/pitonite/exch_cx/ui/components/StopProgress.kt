package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun StopProgress(onClick: () -> Unit, contentDescription: String, size: Dp = 32.dp) {
  Box(contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = MaterialTheme.colorScheme.surfaceVariant,
        trackColor = MaterialTheme.colorScheme.secondary,
    )

    IconButton(onClick = onClick) {
      Icon(imageVector = Icons.Default.Stop, contentDescription = contentDescription, modifier = Modifier.size(size.times(0.8f)))
    }
  }
}

@Preview("default")
@Composable
fun OrdersPreview() {
  ExchTheme {
    Surface() {
      StopProgress({}, "", 32.dp)
    }
  }
}
