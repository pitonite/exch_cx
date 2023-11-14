package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.currentSpAsDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tip(
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
  Card {
    Column(
        modifier.padding(
            top = dimensionResource(R.dimen.padding_xl),
        )) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier =
                  Modifier.padding(horizontal = dimensionResource(R.dimen.padding_lg))
                      .padding(bottom = dimensionResource(R.dimen.padding_sm)),
          ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = stringResource(R.string.tip),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(currentSpAsDp().times(1.4f)),
            )
            Text(stringResource(R.string.tip))
          }
          Text(
              text,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = dimensionResource(R.dimen.padding_xl)))
          if (onDismiss != null) {
            Row {
              Spacer(Modifier.weight(1f))
              TextButton(
                  onClick = onDismiss,
                  modifier =
                      Modifier.padding(bottom = dimensionResource(R.dimen.padding_sm))
                          .padding(end = dimensionResource(R.dimen.padding_xl))) {
                    Text(stringResource(R.string.dismiss))
                  }
            }
          }
        }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun TipPreview() {
  ExchTheme(darkTheme = true) {
    Tip(
        text =
            "Lorem ipsum dolor sit amet consectetur adipisicing elit. Quasi molestias officiis alias blanditiis...",
    ) {
      // on dismiss
    }
  }
}
