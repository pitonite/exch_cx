package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import io.github.pitonite.exch_cx.R

@Composable
fun TextWithLoading(
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
  val annotatedDesc = buildAnnotatedString {
    append(text)
    append("  ")
    pushStringAnnotation(tag = "indicator", annotation = "indicator")
    appendInlineContent("indicator", "([${stringResource(R.string.accessibility_label_working)}])")
    pop()
  }
  val indicatorSize = LocalTextStyle.current.fontSize
  Text(
      annotatedDesc,
      modifier = modifier,
      fontSize = fontSize,
      color = color,
      inlineContent =
          mapOf(
              Pair(
                  "indicator",
                  InlineTextContent(
                      Placeholder(
                          width = indicatorSize,
                          height = indicatorSize,
                          placeholderVerticalAlign = PlaceholderVerticalAlign.Center)) {
                        CircularProgressIndicator()
                      },
              )))
}
