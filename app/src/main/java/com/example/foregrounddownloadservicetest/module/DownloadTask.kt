package com.example.foregrounddownloadservicetest.module

import android.app.*
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foregrounddownloadservicetest.R
import com.example.foregrounddownloadservicetest.module.retrofit.DownloadFileRepository
import java.io.*
import java.lang.Exception
import javax.inject.Inject

private const val TAG = "DownloadTask"
const val SUCCEED = 100
const val FAILED = -1
const val UNZIP = 101

class DownloadTask(private val context: Context, private val downloadFileRepository : DownloadFileRepository, private val downloadInfo: DownloadInfo) : DownloadSubject,
    FileWriteObserver {
    private var notificationManager: NotificationManager =
        context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    var notificationId: Int = NotificationController.getUnUsedNotificationId(notificationManager)
        private set
    private val observerList: MutableList<DownloadObserver> = arrayListOf()
    var notificationBuilder: NotificationCompat.Builder
    private var progress: Int = 0

    constructor(context: Context, downloadFileRepository : DownloadFileRepository, downloadInfo: DownloadInfo, notificationId: Int) : this(
        context,
        downloadFileRepository,
        downloadInfo
    ) {
        this.notificationId = notificationId
    }

    init {
        NotificationController.settingNotificationChannel(notificationManager, false)
        notificationBuilder = NotificationController.createDownloadNotificationBuilder(
            context,
            File(downloadInfo.filePath).name
        )
    }

    //handle download observer

    override fun registerObserver(observer: DownloadObserver) {
        observerList.add(observer)
    }

    override fun removeObserver(observer: DownloadObserver) {
        observerList.remove(observer)
    }

    override fun notifyObservers(notificationId: Int, result: Int) {
        observerList.forEach { it.update(notificationId, result) }
    }

    //handle file write observer
    override fun updateFileWriteProgress(progress: Int) {
        this.progress = progress
        if (progress != SUCCEED) notificationUpdate()
    }

    //--

    suspend fun startDownload() {
        Log.d(TAG, "startDownload: ")
        try {
            val response = downloadFileRepository.downloadFile(downloadInfo.url)
            if (response != null && FileController.writeResponseBodyToDisk(this, downloadInfo.filePath, response)) {
                //檢查檔案下載是否完全
                if (FileController.compareFileSizeWithUrl(
                        downloadInfo.filePath,
                        downloadInfo.url
                    )
                ) {
                    //解壓縮
                    if (downloadInfo.upZip) {
                        try {
                            progress = UNZIP
                            notificationUpdate()
                            val unZipFile = File(downloadInfo.filePath)
                            FileController.unZipFolder(
                                downloadInfo.filePath,
                                unZipFile.parent!!
                            )
                            //unzip completed, delete zip file
                            unZipFile.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            progress = FAILED
                        }
                    }
                } else {
                    progress = FAILED
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (progress == UNZIP) progress = SUCCEED
        if (progress != SUCCEED) progress = FAILED
        notificationUpdate()
    }

    private fun notificationUpdate() {
        Log.d(TAG, "notificationUpdate: progress: $progress")
        when (progress) {
            SUCCEED -> {
                //succeed
                notificationBuilder
                    .setContentText(context.getString(R.string.download_succeed))
                    .setProgress(100, 100, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
                Log.d(TAG, "notificationUpdate: succeed notify")
            }
            FAILED -> {
                //failed
                notificationBuilder
                    .setContentText(context.getString(R.string.download_failed))
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
                Log.d(TAG, "notificationUpdate: failed notify")
            }
            UNZIP -> {
                //zip
                notificationBuilder.setProgress(0, 0, false)
                    .setContentText(context.getString(R.string.file_unzipping))
            }
            else -> {
                //update
                notificationBuilder.setProgress(100, progress, false)
            }
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
        notifyObservers(notificationId, progress)
    }
}