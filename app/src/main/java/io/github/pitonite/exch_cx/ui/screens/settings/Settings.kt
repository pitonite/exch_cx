package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.components.UpBtn
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(upPress: () -> Unit, modifier: Modifier = Modifier) {
  val focusManager = LocalFocusManager.current

  Scaffold(
      modifier = modifier,
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = { UpBtn(upPress) },
        )
      }) { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .padding(
                        dimensionResource(R.dimen.page_padding),
                    )
                    .verticalScroll(rememberScrollState())
                    .noRippleClickable() { focusManager.clearFocus() },
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
        ) {

        }
      }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun SettingsPreview() {
  ExchTheme { Settings(upPress = {}) }
}
