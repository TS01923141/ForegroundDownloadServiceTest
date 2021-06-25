package com.example.foregrounddownloadservicetest.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.foregrounddownloadservicetest.BuildConfig
import com.example.foregrounddownloadservicetest.R
import java.util.*
import kotlin.math.abs

private const val CHANNEL_ID = BuildConfig.APPLICATION_ID + "_Channel"
object NotificationController {

    fun getUnUsedNotificationId(notificationManager: NotificationManager): Int {
        val notificationId = abs(Random().nextInt())
        return if (!checkNotificationIdUnUsed(notificationManager, notificationId)) {
            notificationId
        } else {
            getUnUsedNotificationId(notificationManager)
        }
    }

    private fun checkNotificationIdUnUsed(
        notificationManager: NotificationManager,
        notificationId: Int
    ): Boolean {
        var isUsed = false
        for (notification in notificationManager.activeNotifications) {
            if (notification.id == notificationId) {
                isUsed
            }
            break
        }
        return isUsed
    }

    fun settingNotificationChannel(notificationManager: NotificationManager, vibrate: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "channel_name",
                NotificationManager.IMPORTANCE_LOW
            )
            if (!vibrate) {
                //關閉震動
                channel.vibrationPattern = longArrayOf(0)
                channel.enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createDownloadNotificationBuilder(
        context: Context,
        fileName: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText(context.getString(R.string.downloading))
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setOnlyAlertOnce(true) //只提醒一次避免Android O 的無法禁止震動問題
            .setProgress(100, 0, false)
            .setWhen(System.currentTimeMillis())
    }
}