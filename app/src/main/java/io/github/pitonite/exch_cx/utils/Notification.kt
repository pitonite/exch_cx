package io.github.pitonite.exch_cx.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import io.github.pitonite.exch_cx.R

fun isNotificationAllowed(context: Context, channelId: String? = null): Boolean {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (!isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)) return false
  }
  if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    if (channelId == null) return true
    runCatching {
      val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = mgr.getNotificationChannel(channelId)
      return channel.importance != NotificationManager.IMPORTANCE_NONE
    }
    return false
  } else {
    return false
  }
}

fun createNotificationChannels(context: Context) {
  val notifManager = NotificationManagerCompat.from(context)

  notifManager.createNotificationChannel(
      NotificationChannelCompat.Builder(
              context.getString(R.string.channel_id_order_state_change),
              NotificationManagerCompat.IMPORTANCE_HIGH)
          .setName(context.getString(R.string.channel_name_order_state_change))
          .build())

  notifManager.createNotificationChannel(
      NotificationChannelCompat.Builder(
              context.getString(R.string.channel_id_order_support),
              NotificationManagerCompat.IMPORTANCE_HIGH)
          .setName(context.getString(R.string.channel_name_order_support))
          .build())
}
