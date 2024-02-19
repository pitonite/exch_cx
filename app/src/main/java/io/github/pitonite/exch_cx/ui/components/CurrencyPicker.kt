package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect.CurrencySelect
import io.github.pitonite.exch_cx.utils.nonScaledSp
import java.math.BigDecimal
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    modifier: Modifier = Modifier,
    currency: String? = null,
    enabled: Boolean = true,
    currencyList: PersistentList<CurrencyDetail>,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    showReserves: Boolean = false,
    onCurrencySelected: (CurrencyDetail) -> Unit = {},
    showReserveAlertTip: Boolean = false,
    onReserveAlertTipDismissed: () -> Unit = {},
    showSelectDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    textSize: TextUnit = 23.sp.nonScaledSp,
    contentPadding: PaddingValues =
        PaddingValues(
            start = dimensionResource(R.dimen.padding_sm),
            top = dimensionResource(R.dimen.padding_sm),
            bottom = dimensionResource(R.dimen.padding_sm),
        ),
) {

  Button(
      onClick = { showSelectDialog.value = true },
      modifier = modifier,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onSurface,
              disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          ),
      enabled = enabled,
      shape = MaterialTheme.shapes.extraSmall,
      contentPadding = contentPadding,
  ) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      ExchDrawable(
          name =
              if (currency.isNullOrEmpty()) {
                "generic"
              } else currency.lowercase(),
          colorFilter =
              if (!enabled) {
                val matrix = ColorMatrix()
                matrix.setToSaturation(0.4f)
                ColorFilter.colorMatrix(matrix)
              } else null,
      )
      Text(
          modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_sm)),
          text =
              if (currency.isNullOrEmpty()) {
                "UKWN"
              } else currency.uppercase(),
          fontSize = textSize,
      )
      Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = stringResource(R.string.dropdown_arrow),
          modifier = Modifier.size(30.dp),
      )
    }
  }

  if (showSelectDialog.value) {
    BasicAlertDialog(
        onDismissRequest = { showSelectDialog.value = false },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
      CurrencySelect(
          modifier = modifier.fillMaxSize().imePadding(),
          topBar = {
            TopAppBar(
                title = title,
                navigationIcon = { UpBtn { showSelectDialog.value = false } },
                actions = actions,
            )
          },
          aboveSearch = {
            ReserveAlertTip(
                visible = showReserveAlertTip, onTipDismissed = onReserveAlertTipDismissed)
          },
          currencyList = currencyList,
          onCurrencySelected = {
            onCurrencySelected(it)
            showSelectDialog.value = false
          },
          showReserves = showReserves,
      )
    }
  }
}

@Preview("default")
@Composable
fun CurrencyPickerPreview() {
  Surface {
    CurrencyPicker(
        currency = "btc",
        title = { Text("Custom Title") },
        showReserves = true,
        actions = {},
        onCurrencySelected = {},
        showReserveAlertTip = false,
        onReserveAlertTipDismissed = {},
        currencyList =
            persistentListOf(
                CurrencyDetail("btc", reserve = BigDecimal.ONE),
                CurrencyDetail("eth", reserve = BigDecimal.TEN),
            ),
        enabled = true,
    )
  }
}

@Preview("default")
@Composable
fun CurrencyPickerDialogPreview() {
  Surface {
    CurrencyPicker(
        currency = "btc",
        title = { Text("Custom Title") },
        showReserves = true,
        actions = {},
        onCurrencySelected = {},
        showReserveAlertTip = false,
        onReserveAlertTipDismissed = {},
        currencyList =
            persistentListOf(
                CurrencyDetail("btc", reserve = BigDecimal.ONE),
                CurrencyDetail("eth", reserve = BigDecimal.TEN),
            ),
        enabled = true,
        showSelectDialog = remember { mutableStateOf(true) },
    )
  }
}
