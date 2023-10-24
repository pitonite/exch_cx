package io.github.pitonite.exch_cx.model

// from
// https://afigaliyev.medium.com/snackbar-state-management-best-practices-for-jetpack-compose-1a5963d86d98
// with edits

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Represents a message to be displayed in a snackbar.
 *
 * @property id a unique id for this snack message.
 */
@Immutable
sealed interface SnackbarMessage {
  val id: Long
  /**
   * Represents a text message to be displayed in a snackbar.
   *
   * @property message text to be shown in the Snackbar.
   * @property actionLabel optional action label to show as button in the Snackbar.
   * @property withDismissAction a boolean to show a dismiss action in the Snackbar. This is
   *   recommended to be set to true better accessibility when a Snackbar is set with a
   *   [SnackbarDuration.Indefinite].
   * @property duration duration of the Snackbar.
   * @property onSnackbarResult A callback for when the snackbar is dismissed or the action is
   *   performed.
   */
  @Immutable
  data class Text(
      override val id: Long,
      val message: UserMessage,
      val actionLabel: UserMessage? = null,
      val withDismissAction: Boolean = false,
      val duration: SnackbarDuration = SnackbarDuration.Short,
      val onSnackbarResult: (SnackbarResult) -> Unit = {}
  ) : SnackbarMessage

  /**
   * Represents a message with custom visuals to be displayed in a snackbar.
   *
   * @property snackbarVisuals Custom visuals for the snackbar.
   * @property onSnackbarResult A callback for when the snackbar is dismissed or the action is
   *   performed.
   */
  @Immutable
  data class Visuals(
      override val id: Long,
      val snackbarVisuals: SnackbarVisuals,
      val onSnackbarResult: (SnackbarResult) -> Unit = {}
  ) : SnackbarMessage

  companion object {
    /**
     * Returns a new [SnackbarMessage.Text] instance.
     *
     * @param message text to be shown in the Snackbar.
     * @param actionLabelMessage optional action label to show as button in the Snackbar.
     * @param withDismissAction a boolean to show a dismiss action in the Snackbar. This is
     *   recommended to be set to true better accessibility when a Snackbar is set with a
     *   [SnackbarDuration.Indefinite].
     * @param duration duration of the Snackbar.
     * @param onSnackbarResult A callback for when the snackbar is dismissed or the action is
     *   performed.
     * @return a [Text] instance of [SnackbarMessage].
     */
    fun from(
        message: UserMessage,
        actionLabelMessage: UserMessage? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onSnackbarResult: (SnackbarResult) -> Unit = {}
    ) =
        Text(
            id = UUID.randomUUID().mostSignificantBits,
            message = message,
            actionLabel = actionLabelMessage,
            withDismissAction = withDismissAction,
            duration = duration,
            onSnackbarResult = onSnackbarResult)

    /**
     * Returns a new [SnackbarMessage.Visuals] instance.
     *
     * @param snackbarVisuals Custom visuals for the snackbar.
     * @param onSnackbarResult A callback for when the snackbar is dismissed or the action is
     *   performed.
     * @return a [Visuals] instance of [SnackbarMessage].
     */
    fun from(snackbarVisuals: SnackbarVisuals, onSnackbarResult: (SnackbarResult) -> Unit) =
        Visuals(
            id = UUID.randomUUID().mostSignificantBits,
            snackbarVisuals = snackbarVisuals,
            onSnackbarResult = onSnackbarResult)
  }
}
