package io.github.pitonite.exch_cx.worker

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.pitonite.exch_cx.data.room.ExchDatabase
import io.github.pitonite.exch_cx.di.getUserSettingsDataStoreFile
import io.github.pitonite.exch_cx.utils.extractZip
import io.github.pitonite.exch_cx.utils.jsonFormat

const val restoreWorkName = "restore_work"

@HiltWorker
class RestoreWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val exchDatabase: ExchDatabase,
) : CoroutineWorker(context, workerParams) {

  companion object {
    const val TAG: String = "RestoreWorker"
  }

  override suspend fun doWork(): Result {
    val fileUri = inputData.getString("FILE_URI")?.toUri()
    if (fileUri == null) {
      Log.e(TAG, "FILE_URI not provided to RestoreWorker, aborting restore.")
      return Result.failure()
    }

    val context = this.applicationContext

    val backupDir = context.filesDir.resolve("backup")
    if (backupDir.exists() && !backupDir.deleteRecursively()) {
      Log.e(TAG, "Failed to clear old backup directory in context.filesDir, aborting restore.")
      return Result.failure()
    }
    if (!backupDir.mkdirs()) {
      Log.e(TAG, "Failed to create backup directory in context.filesDir, aborting restore.")
      return Result.failure()
    }

    context.contentResolver.openInputStream(fileUri).use { fis ->
      if (fis == null) {
        Log.e(TAG, "Failed to open input stream to $fileUri, aborting restore.")
        return Result.failure()
      }

      extractZip(
          fis,
          backupDir,
      )

      try {
        val dataStore = getUserSettingsDataStoreFile(context)
        val settingsBackupPath = backupDir.resolve("user_settings.pb")
        settingsBackupPath.copyTo(dataStore, true)
      } catch (e: Throwable) {
        Log.e(TAG, "Failed to restore user settings, error: ${e.message}")
      }

      try {
        val dbBackupPath = backupDir.resolve("database.json")
        val databaseJSON = dbBackupPath.readText(Charsets.UTF_8)
        val backupData = jsonFormat.decodeFromString(BackupData.serializer(), databaseJSON)

        exchDatabase.ordersDao().insertAll(backupData.orders)
        exchDatabase.supportMessagesDao().insertAll(backupData.supportMessages)
        exchDatabase.currencyReserveDao().insertAll(backupData.reserves)
        exchDatabase.currencyReserveTriggerDao().insertAll(backupData.triggers) // to not replace current triggers
      } catch (e: Throwable) {
        Log.e(TAG, "Failed to restore database, error: ${e.message}")
      }
    }

    backupDir.deleteRecursively()

    return Result.success()
  }
}
