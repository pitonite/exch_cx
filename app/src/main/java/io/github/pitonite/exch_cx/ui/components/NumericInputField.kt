package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.github.pitonite.exch_cx.utils.NumberFormatter

@Composable
fun NumericInputField(
    modifier: Modifier = Modifier,
    value: String,
    label: @Composable () -> Unit,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
    minValue: Int? = null,
    maxValue: Int? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
) {
  val focusManager = LocalFocusManager.current

  var hadFocus by remember { mutableStateOf(false) }

  OutlinedTextField(
      label = label,
      textStyle = textStyle,
      modifier =
          modifier.onFocusChanged {
            if (!it.hasFocus && hadFocus) {
              hadFocus = false
              if (value.isEmpty()) {
                onValueChange(value)
              } else {

                val numericValue = value.toIntOrNull()
                if (minValue !== null && numericValue !== null && numericValue < minValue) {
                  onValueChange(minValue.toString())
                } else if (maxValue !== null && numericValue !== null && numericValue > maxValue) {
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
        val cleanedInput = NumberFormatter.cleanup(it)

        if (maxValue !== null) {
          val numericValue = cleanedInput.toIntOrNull() ?: 0
          if (numericValue > maxValue) {
            onValueChange(maxValue.toString())
            return@OutlinedTextField
          }
        }
        onValueChange(NumberFormatter.cleanup(it))
      },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
      keyboardActions =
          KeyboardActions(
              onDone = { focusManager.clearFocus() },
              onNext = { focusManager.moveFocus(FocusDirection.Next) }),
      singleLine = true,
  )
}
