package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val CHANNEL_ID = "backup_channel"
    private val NOTIFICATION_ID = 1

    override suspend fun doWork(): Result {
        return try {
            // Define database path and backup path
            val dbPath = applicationContext.getDatabasePath("restaurant.db").absolutePath
            val backupPath = File(applicationContext.getExternalFilesDir(null), "backup/restaurant_backup.db").absolutePath

            // Perform the backup
            backupDatabase(dbPath, backupPath)

            Log.d("BackupWorker", "Database backed up successfully.")

            // Show notification after successful backup
            showNotification("Backup Complete", "Database has been backed up successfully.")

            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Database backup failed", e)
            Result.failure()
        }
    }

    private fun backupDatabase(sourcePath: String, destinationPath: String) {
        val inputChannel = FileInputStream(sourcePath).channel
        val outputChannel = FileOutputStream(destinationPath).channel
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
        inputChannel.close()
        outputChannel.close()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel if running on Android 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Database Backup Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for database backup status"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // You can use your own icon here
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
