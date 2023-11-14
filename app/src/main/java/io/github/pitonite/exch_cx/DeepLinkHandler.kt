package io.github.pitonite.exch_cx

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.ui.navigation.getOrderDetailUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Stable
class DeepLinkHandler @Inject constructor() {
  val event = MutableStateFlow<Event>(Event.None)

  fun handleDeepLink(intent: Intent?) {
    if (intent != null) {
      // make a copy
      val intent = Intent(intent)
      // this is to remove the FLAG_ACTIVITY_NEW_TASK flag, because navigation bugs otherwise
      intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP and Intent.FLAG_ACTIVITY_CLEAR_TOP

      event.update { Event.NavigateWithDeepLink(intent) }
    }
  }

  fun consumeEvent() {
    event.update { Event.None }
  }
}

sealed interface Event {

  @Stable data class NavigateWithDeepLink(val intent: Intent) : Event

  object None : Event
}

fun getOrderDeepLinkPendingIntent(context: Context, orderid: String): PendingIntent {
  val routeIntent =
      Intent(Intent.ACTION_VIEW, getOrderDetailUri(orderid)).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP and Intent.FLAG_ACTIVITY_CLEAR_TOP
      }

  val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

  return PendingIntent.getActivity(context, 0, routeIntent, flags)
}
