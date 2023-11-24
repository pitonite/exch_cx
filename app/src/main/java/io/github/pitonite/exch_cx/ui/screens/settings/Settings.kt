package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import io.github.pitonite.exch_cx.PreferredProxyType
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.NumericInputField
import io.github.pitonite.exch_cx.ui.components.RadioGroup
import io.github.pitonite.exch_cx.ui.components.RadioGroupRow
import io.github.pitonite.exch_cx.ui.components.UpBtn
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.noRippleClickable
import io.github.pitonite.exch_cx.utils.verticalFadingEdge
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(viewModel: SettingsViewModel, upPress: () -> Unit, modifier: Modifier = Modifier) {
  val focusManager = LocalFocusManager.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
  val scrollState = rememberScrollState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

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
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        TopAppBar(
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseOnSurface),
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = { UpBtn(upPress) },
        )
      },
      contentWindowInsets =
          ScaffoldDefaults.contentWindowInsets
              .exclude(WindowInsets.navigationBars)
              .exclude(WindowInsets.ime),
  ) { padding ->
    Column(
        modifier =
            modifier
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    horizontal = dimensionResource(R.dimen.page_padding),
                )
                .verticalFadingEdge(scrollState, dimensionResource(R.dimen.fading_edge))
                .verticalScroll(scrollState)
                .noRippleClickable() { focusManager.clearFocus() },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // region autoupdate
      Card {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_xl)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
        ) {
          Text(text = stringResource(R.string.title_background_update), fontSize = 20.sp)

          SettingItemSwitch(
              text = stringResource(R.string.label_is_order_auto_update_enabled),
              checked = viewModel.isOrderAutoUpdateEnabledDraft,
              onCheckedChange = { viewModel.updateIsOrderAutoUpdateEnabledDraft(it) },
          )

          val currentPeriod =
              if (viewModel.orderAutoUpdatePeriodMinutesDraft <= 15) 15
              else viewModel.orderAutoUpdatePeriodMinutesDraft

          PeriodSelectionInput(
              value = currentPeriod.toString() + " " + stringResource(R.string.minutes),
              onPeriodSelected = { viewModel.updateOrderAutoUpdatePeriodMinutesDraft(it) },
              enabled = viewModel.isOrderAutoUpdateEnabledDraft,
          )

          SettingItemSwitch(
              text = stringResource(R.string.label_archive_orders_automatically),
              checked = viewModel.archiveOrdersAutomaticallyDraft,
              onCheckedChange = { viewModel.updateArchiveOrdersAutomaticallyDraft(it) },
          )

          SettingItemSwitch(
              text = stringResource(R.string.label_delete_remote_order_data_automatically),
              checked = viewModel.deleteRemoteOrderDataAutomaticallyDraft,
              onCheckedChange = { viewModel.updateDeleteRemoteOrderDataAutomaticallyDraft(it) },
          )

          Button(onClick = { viewModel.saveAutoUpdateSettings() }) {
            Text(stringResource(R.string.label_save))
          }
        }
      }
      // endregion autoupdate

      // region api
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

          Button(onClick = { viewModel.saveRequestSettings() }) {
            Text(stringResource(R.string.label_save))
          }
        }
      }
      // endregion api

      // region proxy
      Card {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_xl)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
          Text(text = stringResource(R.string.label_proxy_settings), fontSize = 20.sp)

          SettingItemSwitch(
              text = stringResource(R.string.label_is_proxy_enabled),
              checked = viewModel.isProxyEnabledDraft,
              onCheckedChange = { viewModel.updateIsProxyEnabledDraft(it) },
          )
          OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              enabled = viewModel.isProxyEnabledDraft,
              value = viewModel.proxyHostDraft,
              onValueChange = { viewModel.updateProxyHostDraft(it) },
              label = { Text(stringResource(R.string.label_proxy_host)) })
          NumericInputField(
              modifier = Modifier.fillMaxWidth(),
              enabled = viewModel.isProxyEnabledDraft,
              value = viewModel.proxyPortDraft,
              minValue = 0,
              maxValue = 65535,
              onValueChange = { viewModel.updateProxyPortDraft(it) },
              label = { Text(stringResource(R.string.label_proxy_port)) })

          Spacer(Modifier.padding(bottom = 10.dp))

          Text(text = stringResource(R.string.label_preferred_proxy_type), fontSize = 20.sp)
          RadioGroupRow(
              enabled = viewModel.isProxyEnabledDraft,
              options = persistentListOf(PreferredProxyType.SOCKS5, PreferredProxyType.HTTP),
              selectedOption = viewModel.preferredProxyTypeDraft,
              onOptionSelected = { viewModel.updatePreferredProxyTypeDraft(it) },
              label = {
                Text(
                    when (it) {
                      PreferredProxyType.SOCKS5 -> stringResource(R.string.label_proxy_type_socks5)
                      PreferredProxyType.HTTP -> stringResource(R.string.label_proxy_type_http)
                      else -> stringResource(R.string.Unknown)
                    })
              })

          Button(onClick = { viewModel.saveProxySettings() }) {
            Text(stringResource(R.string.label_save))
          }
        }
      }
      // endregion proxy

      Spacer(Modifier)
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun SettingsPreview() {
  ExchTheme {
    Settings(
        viewModel = SettingsViewModel(SavedStateHandle(), UserSettingsRepositoryMock()),
        upPress = {})
  }
}
