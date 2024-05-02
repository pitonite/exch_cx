package io.github.pitonite.exch_cx.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking

@Composable
fun RestoreButton(
    modifier: Modifier = Modifier,
    workState: WorkState,
    onFileSelected: (Uri) -> Unit,
) {
  val context = LocalContext.current

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
          try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            onFileSelected(uri)
          } catch (e: Throwable) {
            SnackbarManager.showMessage(
                SnackbarMessage.from(
                    message = UserMessage.from(R.string.snack_restore_permission_failed),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short,
                ),
            )
            return@rememberLauncherForActivityResult
          }
        }
      }

  Button(
      onClick = { launcher.launch(arrayOf("application/zip")) },
      modifier = modifier,
      enabled = !workState.isWorking(),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                dimensionResource(R.dimen.padding_sm),
            ),
    ) {
      Text(stringResource(R.string.label_restore_backup))
      if (workState.isWorking()) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
      }
    }
  }
}

@Preview
@Composable
fun RestoreButtonPreview() {
  ExchTheme { Surface { RestoreButton(workState = WorkState.NotWorking) {} } }
}

@Preview
@Composable
fun RestoreButtonBusyPreview() {
  ExchTheme { Surface { RestoreButton(workState = WorkState.Working()) {} } }
}
