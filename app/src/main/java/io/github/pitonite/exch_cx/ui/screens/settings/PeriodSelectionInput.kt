package io.github.pitonite.exch_cx.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.components.MyReadOnlyTextField
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

val periodOptions =
    persistentListOf<Long>(
        15,
        30,
        60,
        90,
        120,
        240,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelectionInput(
  modifier: Modifier = Modifier,
  value: String,
  onPeriodSelected: (Long) -> Unit,
  enabled: Boolean = true,
  shape: Shape = OutlinedTextFieldDefaults.shape,
  trailingIcon: @Composable (() -> Unit)? = null,
  label: String = stringResource(R.string.label_auto_update_period),
) {
  val scope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState()
  var showBottomSheet by remember { mutableStateOf(false) }

  val toggleModalBottomSheetState: () -> Unit = {
    scope.launch {
      if (showBottomSheet) {
        sheetState.hide()
        showBottomSheet = false
      } else {
        showBottomSheet = true
      }
    }
  }

  LaunchedEffect(showBottomSheet) {
    if (showBottomSheet) {
      sheetState.show()
    }
  }

  MyReadOnlyTextField(
      value = value,
      label = label,
      onClick = { toggleModalBottomSheetState() },
      enabled = enabled,
      shape = shape,
      trailingIcon = trailingIcon,
      modifier = modifier,
  )

  if (showBottomSheet) {
    PeriodSelectionSheet(
        sheetState = sheetState,
        toggleModalBottomSheetState = toggleModalBottomSheetState,
        onPeriodSelected = onPeriodSelected,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelectionSheet(
    sheetState: SheetState = rememberModalBottomSheetState(),
    toggleModalBottomSheetState: () -> Unit,
    onPeriodSelected: (Long) -> Unit,
) {

  ModalBottomSheet(
      onDismissRequest = toggleModalBottomSheetState,
      sheetState = sheetState,
  ) {
    LazyColumn(
        modifier = Modifier.navigationBarsPadding(),
    ) {
      items(periodOptions) {
        Text(
            text = it.toString() + " " + stringResource(R.string.minutes),
            modifier =
                Modifier.fillMaxWidth()
                    .clickable {
                      onPeriodSelected(it)
                      toggleModalBottomSheetState()
                    }
                    .padding(
                        horizontal = dimensionResource(R.dimen.padding_lg),
                        vertical = dimensionResource(R.dimen.padding_lg),
                    ),
            fontSize = 18.sp,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("sheet")
@Composable
fun PeriodSelectionSheetPreview() {
  ExchTheme {
    val density = LocalDensity.current
    PeriodSelectionSheet(
        toggleModalBottomSheetState = {},
        onPeriodSelected = {},
        sheetState =
            SheetState(
                skipPartiallyExpanded = false,
                initialValue = SheetValue.Expanded,
                density = density),
    )
  }
}

@Preview("input")
@Composable
fun PeriodSelectionInputPreview() {
  ExchTheme {
    Surface {
      PeriodSelectionInput(
          value = "15 minutes",
          onPeriodSelected = {},
      )
    }
  }
}
