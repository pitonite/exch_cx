package io.github.pitonite.exch_cx.ui.screens.ordersupport.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

// from https://github.com/android/compose-samples
// from jetchat user input
// with a lot of edits

@Preview
@Composable
fun UserInputPreview() {
  ExchTheme {
    Surface {
      UserInput(value = "", onSendMessage = { }, onValueChanged = {})
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
  modifier: Modifier = Modifier,
  onSendMessage: () -> Unit,
  value: String,
  onValueChanged: (String) -> Unit,
  sendingMessage: Boolean = false,
) {

  // Used to decide if the keyboard should be shown
  var textFieldFocusState by remember { mutableStateOf(false) }

  Surface(
      tonalElevation = 2.dp,
      contentColor = MaterialTheme.colorScheme.secondary,
  ) {
    Column(
        modifier = modifier
            .padding(
                horizontal = dimensionResource(R.dimen.page_padding),
                vertical = dimensionResource(R.dimen.padding_md),
            ),
    ) {

      UserInputText(
          value = value,
          onValueChanged = onValueChanged,
          focusState = textFieldFocusState,
          sendMessageEnabled = value.isNotBlank() && !sendingMessage,
          sendingMessage = sendingMessage,
          keyboardShown = textFieldFocusState,
          onTextFieldFocused = { focused ->
            textFieldFocusState = focused
          },
          onSendMessage = onSendMessage,
      )
    }
  }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@ExperimentalFoundationApi
@Composable
private fun UserInputText(
  keyboardType: KeyboardType = KeyboardType.Text,
  keyboardShown: Boolean,
  onTextFieldFocused: (Boolean) -> Unit,
  onValueChanged: (String) -> Unit,
  value: String,
  focusState: Boolean,
  sendMessageEnabled: Boolean,
  sendingMessage: Boolean,
  onSendMessage: () -> Unit,
) {
  val a11ylabel = stringResource(id = R.string.textfield_desc)
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
      verticalAlignment = Alignment.Bottom,
  ) {

    Box(Modifier.weight(1f).heightIn(min=36.dp)) {
      UserInputTextField(
          value,
          onValueChanged,
          onTextFieldFocused,
          keyboardType,
          focusState,
          Modifier.semantics {
            contentDescription = a11ylabel
            keyboardShownProperty = keyboardShown
          },
      )
    }


    val border = if (!sendMessageEnabled) {
      BorderStroke(
          width = 1.dp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
      )
    } else {
      null
    }

    val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    val buttonColors = ButtonDefaults.buttonColors(
        disabledContainerColor = Color.Transparent,
        disabledContentColor = disabledContentColor,
    )

    // Send button
    Button(
        modifier = Modifier.height(36.dp),
        enabled = sendMessageEnabled,
        onClick = onSendMessage,
        colors = buttonColors,
        border = border,
        contentPadding = PaddingValues(0.dp),
    ) {
      if (sendingMessage) {
        CircularProgressIndicator(Modifier.size(17.dp))
      } else {
        Text(
            stringResource(id = R.string.send),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
    }
  }
}

@Composable
private fun BoxScope.UserInputTextField(
  value: String,
  onValueChanged: (String) -> Unit,
  onTextFieldFocused: (Boolean) -> Unit,
  keyboardType: KeyboardType,
  focusState: Boolean,
  modifier: Modifier = Modifier
) {
  var lastFocusState by remember { mutableStateOf(false) }
  BasicTextField(
      value = value,
      onValueChange = onValueChanged,
      modifier = modifier
          .fillMaxWidth()
          .align(Alignment.CenterStart)
          .padding(end = dimensionResource(R.dimen.padding_xs))
          .onFocusChanged { state ->
            if (lastFocusState != state.isFocused) {
              onTextFieldFocused(state.isFocused)
            }
            lastFocusState = state.isFocused
          },
      keyboardOptions = KeyboardOptions(
          keyboardType = keyboardType,
          imeAction = ImeAction.Send,
      ),
      cursorBrush = SolidColor(LocalContentColor.current),
      textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
  )

  val disableContentColor =
      MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
  if (value.isEmpty() && !focusState) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterStart),
        text = stringResource(R.string.textfield_hint_support_message),
        style = MaterialTheme.typography.bodyLarge.copy(color = disableContentColor),
    )
  }
}
