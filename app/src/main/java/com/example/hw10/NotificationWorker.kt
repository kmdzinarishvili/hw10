package com.example.hw10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters


class NotificationWorker(private val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    private val notificationManager =  appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "CHANNEL_ID"
    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }
    private fun showNotification() {
        createNotificationChannel()
        val notificationId = System.currentTimeMillis().toInt()
        val snoozeIntent = Intent(appContext, CancelBroadcastReceiver::class.java)
            snoozeIntent.putExtra("notificationId", notificationId)
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(appContext, 0, snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val  builder = NotificationCompat.Builder(appContext, channelId)
            .setContentTitle("Notification App")
            .setContentText("You are being notified once every 10 Minutes")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    appContext.resources, R.drawable
                        .ic_launcher_background
                )
            )
            .addAction(R.drawable.ic_launcher_foreground, "Cancel",
                snoozePendingIntent)
        notificationManager.notify(notificationId, builder.build())
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    class CancelBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.extras?.getInt("notificationId")
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id!!)
            WorkManager.getInstance(context).cancelAllWorkByTag("cancelable")
        }

    }

}