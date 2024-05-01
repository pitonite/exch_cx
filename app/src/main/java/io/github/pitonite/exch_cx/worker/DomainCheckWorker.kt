package io.github.pitonite.exch_cx.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.pitonite.exch_cx.MainActivity
import io.github.pitonite.exch_cx.PreferredDomainType
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.UserSettingsRepository
import io.github.pitonite.exch_cx.model.api.ConnectivityTest
import io.github.pitonite.exch_cx.network.CLEARNET_HOSTS
import io.github.pitonite.exch_cx.network.HCHttpClient
import io.github.pitonite.exch_cx.network.getExchDomain
import io.github.pitonite.exch_cx.utils.createNotificationChannels
import io.github.pitonite.exch_cx.utils.isNotificationAllowed
import io.ktor.client.call.body
import java.util.Date
import okhttp3.internal.closeQuietly

const val domainCheckWorkName = "check_domain_work"

@HiltWorker
class DomainCheckWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userSettingsRepository: UserSettingsRepository,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG: String = "DomainCheckWorker"
  }

  override suspend fun doWork(): Result {
    if (!isNetworkAvailable(this.applicationContext)) {
      Log.i(TAG, "Skipping DomainCheckWorker at ${Date()} due to unavailable network")
      return Result.failure()
    }
    Log.i(TAG, "Starting DomainCheckWorker at ${Date()}")

    val context = this.applicationContext

    val notificationAllowed = isNotificationAllowed(context)
    val notifManager: NotificationManagerCompat?

    if (notificationAllowed) {
      notifManager = NotificationManagerCompat.from(context)
      createNotificationChannels(context)
    } else {
      Log.e(TAG, "sending notifications is not allowed, doing domain check silently.")
      notifManager = null
    }
    val userSettings = userSettingsRepository.fetchSettings()
    val selectedIndex = userSettings.selectedClearnetDomainIndex
    var checkIndex = selectedIndex
    do {
      val hcHttpClient = HCHttpClient(userSettings, checkIndex)
      try {
        hcHttpClient.get("/connectivity_test").body<ConnectivityTest>()
        if (checkIndex != selectedIndex) {
          userSettingsRepository.setSelectedClearnetDomainIndex(checkIndex)
          notifyChange(notifManager, checkIndex)
        }
        Log.i(
            TAG,
            "Successfully received OK from ${CLEARNET_HOSTS[checkIndex]}.")
        return Result.success()
      } catch (e: Throwable) {
        checkIndex += 1
        if (checkIndex >= CLEARNET_HOSTS.size) {
          checkIndex = 0
        }
      } finally {
        hcHttpClient._client.closeQuietly()
      }
    } while (checkIndex != selectedIndex)

    return Result.failure()
  }

  @SuppressLint("MissingPermission")
  private fun notifyChange(notifManager: NotificationManagerCompat?, newDomainIndex: Int) {
    if (notifManager != null) {
      val context = this.applicationContext

      val notifTag = "domain_check"

      val notifBuilder =
          NotificationCompat.Builder(context, context.getString(R.string.channel_id_domain_check))
              .setSmallIcon(R.drawable.x_large)
              .setContentTitle(
                  context.getString(
                      R.string.channel_name_domain_check,
                  ),
              )
              .setContentText(
                  context.getString(
                      R.string.clearnet_domain_changed,
                      getExchDomain(PreferredDomainType.NORMAL, newDomainIndex)),
              )
              .setContentIntent(
                  PendingIntentCompat.getActivity(
                      context,
                      0,
                      Intent(context, MainActivity::class.java),
                      PendingIntent.FLAG_UPDATE_CURRENT,
                      false,
                  ),
              )
              .setAutoCancel(true)

      notifManager.notify(notifTag, R.id.notif_id_domain_check, notifBuilder.build())
    }
  }

  private fun isNetworkAvailable(context: Context?): Boolean {
    if (context == null) return false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val capabilities =
          connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
      if (capabilities != null) {
        when {
          capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            return true
          }
          capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
            return true
          }
          capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
            return true
          }
        }
      }
    } else {
      val activeNetworkInfo = connectivityManager.activeNetworkInfo
      if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        return true
      }
    }
    return false
  }
}
