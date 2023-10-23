package io.github.pitonite.exch_cx.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun History(
    onOrderSelected: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun HistoryPreview() {
  ExchTheme { History(onOrderSelected = {}, onNavigateToRoute = {}) }
}
