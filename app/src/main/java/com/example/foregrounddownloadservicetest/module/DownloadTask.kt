package com.example.foregrounddownloadservicetest.module

import android.app.*
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foregrounddownloadservicetest.R
import com.example.foregrounddownloadservicetest.module.retrofit.DownloadFileRepository
import okhttp3.ResponseBody
import java.io.*
import java.lang.Exception
import java.net.SocketException
import java.util.*
import kotlin.math.abs

private const val TAG = "DownloadTask"
private const val CHANNEL_ID = "DownloadRepositoryChannel"
const val SUCCEED = 100
const val FAILED = -1
const val UNZIP = 101

class DownloadTask(private val context: Context, private val downloadInfo: DownloadInfo) : Subject {
    private var notificationManager: NotificationManager =
        context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    var notificationId: Int = getUnUsedNotificationId()
        private set
    private val observerList: MutableList<Observer> = arrayListOf()
    var notificationBuilder: NotificationCompat.Builder
    private var fileSize: Long = 0
    private var downloadSize: Long = 0
    private var progress: Int = 0

    constructor(context: Context, downloadInfo: DownloadInfo, notificationId: Int) : this(
        context,
        downloadInfo
    ) {
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

    override fun notifyObservers(notificationId: Int, result: String) {
        observerList.forEach { it.update(notificationId, result) }
    }

    suspend fun startDownload() {
        Log.d(TAG, "startDownload: ")
        try {
            val response = DownloadFileRepository.downloadFile(downloadInfo.url)
            if (response != null) {
                writeResponseBodyToDisk(response)
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
                            Log.d(TAG, "startDownload: unZipStart")
                            FileController.UnZipFolder(
                                downloadInfo.filePath,
                                File(downloadInfo.filePath).parent!!
                            )
                            Log.d(TAG, "startDownload: unZipFinish")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            progress = FAILED
                        }
                    }
                } else {
                    progress = FAILED
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (progress == UNZIP) progress = SUCCEED
        if (progress != SUCCEED) progress = FAILED
        notificationUpdate()
    }

    private fun getUnUsedNotificationId(): Int {
        val notificationId = abs(Random().nextInt())
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

        if (progress == SUCCEED) notifyObservers(
            notificationId,
            context.getString(R.string.download_succeed)
        )
        if (progress == FAILED) notifyObservers(
            notificationId,
            context.getString(R.string.download_failed)
        )
    }

    private fun settingNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                updateProgress()
            }
            outputStream.flush()
            downloadSize == fileSize
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: SocketException) {
            e.printStackTrace()
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
//            Log.d(TAG, "updateProgress: progress: $progress")
            if (progress != SUCCEED) notificationUpdate()
        }
    }
}