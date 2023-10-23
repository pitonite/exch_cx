package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.DecimalFormatter
import io.github.pitonite.exch_cx.utils.DecimalInputVisualTransformation
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

// decimal formatting from
// https://dev.to/tuvakov/decimal-input-formatting-with-jetpack-composes-visualtransformation-110n
// with edits

private enum class DecimalInputPhase {
  // Text field is focused
  Focused,

  // Text field is not focused and input text is empty
  UnfocusedEmpty,

  // Text field is not focused but input text is not empty
  UnfocusedNotEmpty
}

@Composable
fun DecimalInputField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit = {},
    decimalFormatter: DecimalFormatter =
        DecimalFormatter(symbols = DecimalFormatSymbols(Locale.US)),
    textStyle: TextStyle =
        LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
    minValue: BigDecimal? = null,
    maxValue: BigDecimal? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val focusManager = LocalFocusManager.current
  val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

  var hadFocus by remember { mutableStateOf(false) }

  val isFocused = interactionSource.collectIsFocusedAsState().value
  val inputState =
      when {
        isFocused -> DecimalInputPhase.Focused
        value.isEmpty() -> DecimalInputPhase.UnfocusedEmpty
        else -> DecimalInputPhase.UnfocusedNotEmpty
      }

  BasicTextField(
      modifier =
          modifier.onFocusChanged {
            if (!it.hasFocus && hadFocus) {
              hadFocus = false
              if (value.isEmpty()) {
                onValueChange(value)
              } else {

                val decimalValue = value.toBigDecimalOrNull()
                if (minValue !== null && decimalValue !== null && decimalValue < minValue) {
                  onValueChange(minValue.toString())
                } else if (maxValue !== null && decimalValue !== null && decimalValue > maxValue) {
                  onValueChange(maxValue.toString())
                } else if (value.endsWith('.')) {
                  onValueChange(value.substringBeforeLast('.'))
                }
              }
              onFocusLost()
            } else if (it.hasFocus) {
              hadFocus = true
            }
          },
      enabled = enabled,
      value = value,
      onValueChange = {
        val cleanedInput = decimalFormatter.cleanup(it)

        if (maxValue !== null) {
          val decimalValue = cleanedInput.toBigDecimalOrNull()
          if (decimalValue !== null && decimalValue > maxValue) {
            onValueChange(maxValue.toString())
            return@BasicTextField
          }
        }
        onValueChange(decimalFormatter.cleanup(it))
      },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
      keyboardActions =
          KeyboardActions(
              onDone = { focusManager.clearFocus() },
              onNext = { focusManager.moveFocus(FocusDirection.Next) }),
      visualTransformation = DecimalInputVisualTransformation(decimalFormatter),
      textStyle = textStyle,
      singleLine = true,
      cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
      decorationBox = { innerTextField ->
        Box(
            modifier =
                Modifier.drawWithContent {
                  drawContent()
                  drawLine(
                      color = lineColor,
                      start =
                          Offset(
                              x = 0f,
                              y = size.height - 1.dp.toPx(),
                          ),
                      end =
                          Offset(
                              x = size.width,
                              y = size.height - 1.dp.toPx(),
                          ),
                      strokeWidth = 1.dp.toPx(),
                  )
                },
            propagateMinConstraints = true,
        ) {
          if (inputState == DecimalInputPhase.UnfocusedEmpty) {
            Text(
                text = placeholder,
                style = textStyle.copy(color = textStyle.color.copy(alpha = 0.5f)),
            )
          }
          innerTextField()
        }
      },
  )
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Preview("rtl", locale = "ar")
@Composable
fun DecimalInputFieldPreview() {
  ExchTheme(darkTheme = true) {
    DecimalInputField(
        value = "01234.5678901234567",
        onValueChange = {},
    )
  }
}

@Preview("placeholder")
@Composable
fun DecimalInputFieldPlaceHolderPreview() {
  ExchTheme(darkTheme = true) {
    DecimalInputField(
        value = "",
        placeholder = "Place Holder",
        onValueChange = {},
    )
  }
}
