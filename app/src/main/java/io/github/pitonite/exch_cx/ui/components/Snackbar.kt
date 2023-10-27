package io.github.pitonite.exch_cx.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.asString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Class responsible for managing Snackbar messages to show on the screen */
object SnackbarManager {

  @Stable val snackbarHostState = SnackbarHostState()

  private val _messages: MutableStateFlow<List<SnackbarMessage>> = MutableStateFlow(emptyList())
  val messages: StateFlow<List<SnackbarMessage>>
    get() = _messages.asStateFlow()

  fun showMessage(snackbarMessage: SnackbarMessage) {
    _messages.update { currentMessages -> currentMessages + snackbarMessage }
  }

  fun setMessageShown(messageId: Long) {
    _messages.update { currentMessages -> currentMessages.filterNot { it.id == messageId } }
  }
}

/**
 * Provides a [SnackbarHostState] to its content.
 *
 * @param content The content that will have access to the [SnackbarHostState]
 */
@Composable
fun ProvideSnackbarHostState(content: @Composable () -> Unit) {
  CompositionLocalProvider(
      LocalSnackbarHostState provides SnackbarManager.snackbarHostState, content = content)
}

/**
 * Static CompositionLocal that provides access to a [SnackbarHostState]. The value of the
 * [LocalSnackbarHostState] is set using the [CompositionLocalProvider] composable. If no
 * [SnackbarHostState] is provided, no error is thrown.
 */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

/** should be called as early on composable stack */
@Composable
fun SnackbarMessageHandler() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(true) {
    coroutineScope.launch {
      SnackbarManager.messages.collect { currentMessages ->
        if (currentMessages.isNotEmpty()) {
          val snackbarMessage = currentMessages[0]

          // Notify the SnackbarManager so it can remove the current message from the list
          SnackbarManager.setMessageShown(snackbarMessage.id)

          // Display the snackbar on the screen. `showSnackbar` is a function
          // that suspends until the snackbar disappears from the screen
          when (snackbarMessage) {
            is SnackbarMessage.Text ->
                SnackbarManager.snackbarHostState
                    .showSnackbar(
                        message = snackbarMessage.message.asString(context),
                        actionLabel = snackbarMessage.actionLabel?.asString(context),
                        withDismissAction = snackbarMessage.withDismissAction,
                        duration = snackbarMessage.duration,
                    )
                    .let(snackbarMessage.onSnackbarResult)
            is SnackbarMessage.Visuals ->
                SnackbarManager.snackbarHostState
                    .showSnackbar(visuals = snackbarMessage.snackbarVisuals)
                    .let(snackbarMessage.onSnackbarResult)
          }
        }
      }
    }
  }
}
