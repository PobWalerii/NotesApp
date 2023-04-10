package com.example.notesapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.CHANNEL_ID

object ServiceNotification {

    fun setNotification(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BackGroundService"
            val descriptionText = "Running in background"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("BackGroundService")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.splash)
            .build()

        return notification

    }
}