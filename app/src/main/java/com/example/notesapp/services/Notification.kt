package com.example.notesapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.notesapp.R

object ServiceNotification {

    fun setNotification(context: Context, id: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Running in background"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(id, id, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, id)
            .setContentTitle(id)
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.splash)
            .build()

    }
}