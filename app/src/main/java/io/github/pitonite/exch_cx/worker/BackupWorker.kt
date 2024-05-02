package io.github.pitonite.exch_cx.worker

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.pitonite.exch_cx.data.room.CurrencyReserve
import io.github.pitonite.exch_cx.data.room.CurrencyReserveTrigger
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.di.getUserSettingsDataStoreFile
import io.github.pitonite.exch_cx.utils.jsonFormat
import io.github.pitonite.exch_cx.utils.zipFiles
import kotlinx.serialization.Serializable

const val backupWorkName = "backup_work"

@HiltWorker
class BackupWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val exchDatabase: ExchDatabase,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG: String = "BackupWorker"
  }

  override suspend fun doWork(): Result {
    val saveUri = inputData.getString("FILE_URI")?.toUri()
    if (saveUri == null) {
      Log.e(TAG, "FILE_URI not provided to BackupWorker, aborting backup.")
      return Result.failure()
    }

    val context = this.applicationContext

    val backupDir = context.filesDir.resolve("backup")
    if (backupDir.exists() && !backupDir.deleteRecursively()) {
      Log.e(TAG, "Failed to clear old backup directory in context.filesDir, aborting backup.")
      return Result.failure()
    }
    if (!context.filesDir.resolve("backup").mkdir()) {
      Log.e(TAG, "Failed to create backup directory in context.filesDir, aborting backup.")
      return Result.failure()
    }

    val dataStore = getUserSettingsDataStoreFile(context)
    val settingsBackupPath = backupDir.resolve("user_settings.pb")
    dataStore.copyTo(settingsBackupPath, true)

    val orders = exchDatabase.ordersDao().getAll()
    val supportMessages = exchDatabase.supportMessagesDao().getAll()
    val reserves = exchDatabase.currencyReserveDao().getAll()
    val reserveTriggers = exchDatabase.currencyReserveTriggerDao().getAll()

    val backupJSON =
        jsonFormat.encodeToString(
            BackupData.serializer(),
            BackupData(orders, supportMessages, reserves, reserveTriggers),
        )
    val dbBackupPath = backupDir.resolve("database.json")
    dbBackupPath.writeText(backupJSON, Charsets.UTF_8)

    val outputStream = context.contentResolver.openOutputStream(saveUri)

    if (outputStream == null) {
      Log.e(TAG, "Failed to open output stream using the provided uri, aborting backup.")
      return Result.failure()
    }

    zipFiles(
        arrayOf(dbBackupPath, settingsBackupPath),
        outputStream,
    )

    backupDir.deleteRecursively()

    return Result.success()
  }
}

@Serializable
@Immutable
data class BackupData(
    val orders: List<Order>,
    val supportMessages: List<SupportMessage>,
    val reserves: List<CurrencyReserve>,
    val triggers: List<CurrencyReserveTrigger>,
)
