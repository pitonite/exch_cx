package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

// from https://gist.github.com/Abhimanyu14/d2a5935b00e4011289a800f6872b3bd5#file-gistfile1-txt

@Composable
fun MyReadOnlyTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
  Box(
      modifier = modifier,
  ) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        label = {
          Text(
              text = label,
          )
        },
        enabled = enabled,
    )
    Box(
        modifier =
            Modifier.matchParentSize()
                .alpha(0f)
                .clickable(
                    onClick = {
                      if (enabled) {
                        onClick()
                      }
                    },
                ),
    )
  }
}

@Preview("default")
@Composable
fun PeriodSelectionInputPreview() {
  ExchTheme {
    Surface {
      MyReadOnlyTextField(
          value = "15 minutes",
          label = "Readonly input",
      ) {}
    }
  }
}
