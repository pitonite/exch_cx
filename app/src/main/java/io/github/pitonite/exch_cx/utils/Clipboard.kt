package io.github.pitonite.exch_cx.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager

fun copyToClipboard(
    context: Context,
    text: String,
    label: String? = null,
    @StringRes confirmationMessage: Int? = R.string.copied,
) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  clipboard.setPrimaryClip(ClipData.newPlainText(label ?: "", text))
  if (confirmationMessage != null) {
    SnackbarManager.showMessage(
        SnackbarMessage.from(
            message = UserMessage.from(confirmationMessage),
            withDismissAction = true,
            duration = SnackbarDuration.Short,
        ))
  }
}
