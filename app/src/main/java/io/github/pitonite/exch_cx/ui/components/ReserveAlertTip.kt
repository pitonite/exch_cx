package io.github.pitonite.exch_cx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R

@Composable
fun ReserveAlertTip(visible: Boolean, onTipDismissed: () -> Unit) {
  AnimatedVisibility(
      visible = visible,
  ) {
    val hintText = buildAnnotatedString {
      append(stringResource(R.string.tip_reserve_alert))
      append(" (")
      // Append a placeholder string "[icon]" and attach an annotation
      // "inlineContent"
      // on it.
      appendInlineContent("icon", "([active notification icon])")
      append(")")
    }
    Row(
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.page_padding)),
    ) {
      Tip(
          text = {
            Text(
                hintText,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_xl)),
                inlineContent =
                    mapOf(
                        Pair(
                            "icon",
                            InlineTextContent(
                                Placeholder(
                                    width = 18.sp,
                                    height = 18.sp,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                                ),
                            ) {
                              Icon(
                                  Icons.Default.NotificationsActive,
                                  contentDescription = stringResource(R.string.alerts),
                              )
                            },
                        ),
                    ),
            )
          },
      ) {
        onTipDismissed()
      }
    }
  }
}

@Preview
@Composable
fun ReserveAlertTipPreview() {
  Surface { ReserveAlertTip(true) {} }
}
