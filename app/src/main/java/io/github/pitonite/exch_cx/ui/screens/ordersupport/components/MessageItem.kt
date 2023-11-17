package io.github.pitonite.exch_cx.ui.screens.ordersupport.components

import android.text.format.DateFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.model.api.SupportMessageSender
import io.github.pitonite.exch_cx.model.getTranslation
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import java.util.Date

// taken from google compose samples jet chat with a lot of edits

@Composable
fun MessageItem(msg: SupportMessage, isPrevMessageByAnother: Boolean) {
  val isUserMe = msg.sender == SupportMessageSender.USER

  val borderColor = if (isUserMe) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
  } else {
    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
  }

  val spaceBetweenAuthors = if (isPrevMessageByAnother) Modifier.padding(top = 8.dp) else Modifier
  val currentLayoutDirection = LocalLayoutDirection.current
  var oppositeDirection =
      if (currentLayoutDirection == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
  val layoutDirection = if (isUserMe) currentLayoutDirection else oppositeDirection
  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Row(modifier = spaceBetweenAuthors) {
      if (isPrevMessageByAnother) {
        // Avatar
        Icon(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(42.dp)
                .border(1.5.dp, borderColor, CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .align(Alignment.Top),
            imageVector = if (msg.sender == SupportMessageSender.SUPPORT) Icons.Default.SupportAgent else Icons.Default.Person,
            contentDescription = null,
        )
      } else {
        // Space under avatar
        Spacer(modifier = Modifier.width(74.dp))
      }

      AuthorAndTextMessage(
          msg = msg,
          isPrevMessageByAnother = isPrevMessageByAnother,
          isUserMe = isUserMe,
          modifier = Modifier
              .padding(end = 16.dp)
              .weight(1f),
      )
    }
  }

}

@Composable
fun AuthorAndTextMessage(
  msg: SupportMessage,
  isUserMe: Boolean,
  isPrevMessageByAnother: Boolean,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    if (isPrevMessageByAnother) {
      AuthorNameTimestamp(msg)
    }
    ChatItemBubble(msg, isUserMe)

  }
}


@Composable
private fun AuthorNameTimestamp(msg: SupportMessage) {
  // Combine author and timestamp for a11y.
  Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
    Text(
        text = msg.sender.getTranslation(),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .alignBy(LastBaseline)
            .paddingFrom(LastBaseline, after = 8.dp), // Space to 1st bubble
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = DateFormat.format("MMM dd, yyyy HH:mm", msg.createdAt).toString(),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.alignBy(LastBaseline),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 15.dp, 15.dp, 15.dp)


@Composable
private fun ChatItemBubble(
  message: SupportMessage,
  isUserMe: Boolean,
) {

  val backgroundBubbleColor = if (isUserMe) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.surfaceVariant
  }

  Column {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
    ) {
      Surface(
          color = backgroundBubbleColor,
          shape = ChatBubbleShape,
      ) {
        Box {
          ClickableMessage(
              message = message,
              isUserMe = isUserMe,
              modifier = Modifier.let {
                if (isUserMe) it.padding(end = 14.dp) else it
              },
          )

          if (isUserMe) {
            if (message.readBySupport) {
              Icon(
                  modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(5.dp),
                  imageVector = Icons.Default.DoneAll,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  contentDescription = stringResource(R.string.accessibility_label_message_has_been_read),
              )
            } else {
              Icon(
                  modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(5.dp),
                  imageVector = Icons.Default.Check,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  contentDescription = stringResource(R.string.accessibility_label_message_has_been_sent),
              )
            }
          }
        }

      }
    }

  }
}


@Composable
private fun ClickableMessage(
  message: SupportMessage,
  isUserMe: Boolean,
  modifier: Modifier = Modifier,
) {
  val uriHandler = LocalUriHandler.current

  val styledMessage = messageFormatter(
      text = message.message,
      primary = isUserMe,
  )

  ClickableText(
      text = styledMessage,
      style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
      modifier = modifier.padding(16.dp),
      onClick = {
        styledMessage
            .getStringAnnotations(start = it, end = it)
            .firstOrNull()
            ?.let { annotation ->
              when (annotation.tag) {
                SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                else -> Unit
              }
            }
      },
  )
}

@Preview
@Composable
fun MessageItemPreview() {
  ExchTheme {
    Surface(Modifier.padding(10.dp)) {
      Column(Modifier.padding(10.dp)) {
        MessageItem(
            msg = SupportMessage(
                orderid = "", index = 0, createdAt = Date(),
                readBySupport = true,
                sender = SupportMessageSender.USER,
                message = "Hello",
            ),
            isPrevMessageByAnother = true,
        )
        Spacer(Modifier.height(10.dp))
        MessageItem(
            msg = SupportMessage(
                orderid = "", index = 0, createdAt = Date(),
                readBySupport = true,
                sender = SupportMessageSender.USER,
                message = "A very long message that fills width",
            ),
            isPrevMessageByAnother = true,
        )
        Spacer(Modifier.height(10.dp))
        MessageItem(
            msg = SupportMessage(
                orderid = "", index = 0, createdAt = Date(),
                readBySupport = true,
                sender = SupportMessageSender.SUPPORT,
                message = "hello",
            ),
            isPrevMessageByAnother = true,
        )
      }
    }
  }
}
