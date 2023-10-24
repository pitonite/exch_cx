package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.FakeUserSettingsRepository
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.RadioGroup
import io.github.pitonite.exch_cx.ui.components.UpBtn
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(viewModel: SettingsViewModel, upPress: () -> Unit, modifier: Modifier = Modifier) {
  val focusManager = LocalFocusManager.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()

  LaunchedEffect(lifecycleState) {
    when (lifecycleState) {
      Lifecycle.State.STARTED,
      Lifecycle.State.RESUMED -> {
        viewModel.reloadSettings()
      }
      else -> {}
    }
  }

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Card {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_xl)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
            ) {
              Text(text = stringResource(R.string.label_api_settings), fontSize = 20.sp)
              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth(),
                  value = viewModel.apiKeyDraft,
                  onValueChange = { viewModel.updateApiKeyDraft(it) },
                  label = { Text(stringResource(R.string.label_api_key)) })

              HorizontalDivider(Modifier.weight(1f).padding(top = 10.dp))
              Spacer(Modifier.padding(bottom = 10.dp))

              Text(text = stringResource(R.string.label_preferred_domain), fontSize = 20.sp)
              RadioGroup(
                  options = listOf(PreferredDomainType.NORMAL, PreferredDomainType.ONION),
                  selectedOption = viewModel.preferredDomainTypeDraft,
                  onOptionSelected = { viewModel.updatePreferredDomainDraft(it) },
                  label = {
                    Text(
                        when (it) {
                          PreferredDomainType.NORMAL -> stringResource(R.string.label_domain_type_normal)
                          PreferredDomainType.ONION ->
                              stringResource(R.string.label_domain_type_onion)
                          else -> stringResource(R.string.Unknown)
                        })
                  })

              Button(onClick = { viewModel.saveChanges() }) {
                Text(stringResource(R.string.label_save))
              }
            }
          }
        }
      }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun SettingsPreview() {
  ExchTheme {
    Settings(
        viewModel = SettingsViewModel(SavedStateHandle(), FakeUserSettingsRepository()),
        upPress = {})
  }
}
