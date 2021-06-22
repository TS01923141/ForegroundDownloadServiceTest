package com.example.foregrounddownloadservicetest.module

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foregrounddownloadservicetest.R
import com.example.foregrounddownloadservicetest.module.DownloadInfo
import com.example.foregrounddownloadservicetest.module.retrofit.DownloadFileRepository
import okhttp3.ResponseBody
import java.io.*
import java.util.*

private const val TAG = "DownloadRepository"
private const val CHANNEL_ID = "DownloadRepositoryChannel"

class DownloadTask(
    private val context: Context,
    private val downloadInfo: DownloadInfo
) : Subject {
    private var notificationManager: NotificationManager =
        context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    var notificationId: Int = getUnUsedNotificationId()
        private set
    private val observerList: MutableList<Observer> = arrayListOf()
    var notificationBuilder: NotificationCompat.Builder
    private var fileSize: Long = 0
    private var downloadSize: Long = 0
    private var progress: Int = 0

    constructor(context: Context, downloadInfo: DownloadInfo, notificationId: Int) : this(context, downloadInfo) {
        this.notificationId = notificationId
    }

    init {
        settingNotificationChannel()
        notificationBuilder = createNotificationBuilder(File(downloadInfo.filePath).name)
    }


    override fun registerObserver(observer: Observer) {
        observerList.add(observer)
    }

    override fun removeObserver(observer: Observer) {
        observerList.remove(observer)
    }

    override fun notifyObservers(notificationId: Int) {
        observerList.forEach { it.update(notificationId) }
    }

    suspend fun startDownload() {
        Log.d(TAG, "startDownload: ")
        val response = DownloadFileRepository.downloadFile(downloadInfo.url)
        if (response != null) {
            writeResponseBodyToDisk(response)
//            TODO("檢查檔案下載是否完全")
////            if (downloadInfo.upZip)
//            TODO("解壓縮")
        }
        if (progress != 100) {
            progress = -1
            notificationUpdate()
        }
    }

    private fun getUnUsedNotificationId(): Int {
        val notificationId = Random().nextInt()
        return if (!checkNotificationIdUnUsed(notificationId)) {
            notificationId
        } else {
            getUnUsedNotificationId()
        }
    }

    private fun checkNotificationIdUnUsed(notificationId: Int): Boolean {
        var isUsed = false
        for (notification in notificationManager.activeNotifications) {
            if (notification.id == notificationId) {
                isUsed
            }
            break
        }
        return isUsed
    }

    private fun notificationUpdate() {
        Log.d(TAG, "notificationUpdate: progress: " + progress)
        when (progress) {
            100 -> {
                //succeed
                notificationBuilder
                    .setContentText(context.getString(R.string.download_succeed))
                    .setProgress(100, 100, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
//                .setContentIntent(getSuccessIntent(position))
//                    .setDeleteIntent(getDeleteIntent(position))
                Log.d(TAG, "notificationUpdate: succeed notify")
            }
            -1 -> {
                //failed
                notificationBuilder
                    .setContentText(context.getString(R.string.download_failed))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
//                    .setContentIntent(getFailureIntent(position))
//                    .setDeleteIntent(getDeleteIntent(position))
                Log.d(TAG, "notificationUpdate: failed notify")
            }
            else -> {
                //update
                notificationBuilder.setProgress(100, progress, false)
            }
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
        if (progress == 100 || progress == -1) notifyObservers(notificationId)
    }

    private fun settingNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationManager =
//                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //關閉震動
            val channel = NotificationChannel(
                CHANNEL_ID,
                "channel_name",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.vibrationPattern = longArrayOf(0)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationBuilder(fileName: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText(context.getString(R.string.downloading))
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setOnlyAlertOnce(true) //只提醒一次避免Android O 的無法禁止震動問題
            .setProgress(100, 0, false)
            .setWhen(System.currentTimeMillis())
    }

//    private fun getDeleteIntent(position: Int): PendingIntent? {
//        val intent = Intent()
//        intent.action = BackgroundDownloadService.ACTION_DOWNLOAD_PROGRESS_DELETE_BROADCAST
//        return PendingIntent.getBroadcast(context, position, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//    }
//
//    private fun getFailureIntent(position: Int): PendingIntent? {
//        val intent = Intent()
//        intent.action = BackgroundDownloadService.ACTION_DOWNLOAD_PROGRESS_DELETE_BROADCAST
//        return PendingIntent.getBroadcast(context, position, intent, PendingIntent.FLAG_ONE_SHOT)
//    }
//
//    private fun getSuccessIntent(position: Int): PendingIntent? {
//        val intent = Intent()
//        intent.action = BackgroundDownloadService.ACTION_DOWNLOAD_PROGRESS_COMPLETED_BROADCAST
//        return PendingIntent.getBroadcast(context, position, intent, PendingIntent.FLAG_ONE_SHOT)
//    }

    //--

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        val downloadFile = File(downloadInfo.filePath)
        if (downloadFile.parentFile != null && !downloadFile.parentFile.exists()) {
            downloadFile.parentFile.mkdirs()
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val fileReader = ByteArray(4096)
            fileSize = body.contentLength()
            inputStream = body.byteStream()
            outputStream = FileOutputStream(downloadFile.absolutePath)
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                downloadSize += read.toLong()
//                Log.d(TAG, "writeResponseBodyToDisk: file download: $downloadSize of $fileSize")
//                Log.d(TAG, "writeResponseBodyToDisk: download percent: " + (1f * downloadSize / fileSize * 100))
                updateProgress()
            }
            outputStream.flush()
            downloadSize == fileSize
        } catch (e: IOException) {
            Log.e(TAG, "writeResponseBodyToDisk: e: " + e.message)
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()
            return true
        }
    }

    private fun updateProgress() {
        if (downloadSize >= fileSize * (progress + 1) / 100) {
            progress = (1f * downloadSize / fileSize * 100).toInt()
            Log.d(TAG, "updateProgress: progress: $progress")
            notificationUpdate()
        }
    }
}