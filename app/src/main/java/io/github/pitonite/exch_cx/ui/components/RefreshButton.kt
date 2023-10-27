package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.utils.rememberRotateInfinite

@Composable
fun RefreshButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    refreshing: Boolean = false,
) {
  val angle by rememberRotateInfinite()
  IconButton(onClick = onClick, enabled = enabled) {
    Icon(
        modifier =
            Modifier.size(32.dp)
                .then(if (refreshing) Modifier.graphicsLayer { rotationZ = angle } else Modifier),
        imageVector = Icons.Default.Refresh,
        contentDescription = stringResource(R.string.refresh),
    )
  }
}
