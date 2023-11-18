package io.github.pitonite.exch_cx.ui.screens.orderdetail.components

import androidx.annotation.StringRes
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.components.ClickableText
import io.github.pitonite.exch_cx.ui.components.CopyableText
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.copyToClipboard

fun getTxidUri(txid: String, currency: String): String {
  return when (val normalizedCurrency = currency.lowercase()) {
    "btc" -> "https://mempool.space/tx/$txid" // or blockchair.com/bitcoin/transaction/:txid
    "eth" -> "https://etherscan.io/tx/$txid"
    else -> "https://blockchair.com/$normalizedCurrency/transaction/$txid"
  }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TransactionText(
    fromCurrency: String,
    txid: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    @StringRes copyConfirmationMessage: Int = R.string.snack_copied,
) {
  val context = LocalContext.current
  val annotatedString = buildAnnotatedString {
    withStyle(
        style = SpanStyle(color = color, fontSize = fontSize),
    ) {
      withAnnotation("url", "url") { append(txid) }
    }
    append("  ")
    pushStringAnnotation(tag = "copy_icon", annotation = "copy_icon")
    appendInlineContent(
        "copy_icon", "([${stringResource(R.string.accessibility_label_copy_icon)}])")
    pop()
  }
  val iconSize = LocalTextStyle.current.fontSize.times(1.5f)
  val urlHandler = LocalUriHandler.current

  SelectionContainer {
    ClickableText(
        text = annotatedString,
        onClick = { offset ->
          annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { span ->
            if (span.tag == "copy_icon") {
              copyToClipboard(context, txid, confirmationMessage = copyConfirmationMessage)
            } else if (span.tag == "url") {
              urlHandler.openUri(getTxidUri(txid, fromCurrency))
            }
          }
        },
        inlineContent =
            mapOf(
                Pair(
                    "copy_icon",
                    InlineTextContent(
                        Placeholder(
                            width = iconSize,
                            height = iconSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center)) {
                          Icon(
                              Icons.Default.ContentCopy,
                              contentDescription =
                                  stringResource(R.string.accessibility_label_copy))
                        },
                )))
  }
}

@Preview
@Composable
fun OrderCreatedPreview() {
  ExchTheme { Surface() { CopyableText("this is a copyable text") } }
}
