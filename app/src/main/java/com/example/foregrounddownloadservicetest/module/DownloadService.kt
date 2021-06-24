package com.example.foregrounddownloadservicetest.module

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

private const val TAG = "DownloadService"

class DownloadService : Service(), Observer {
    //下載任務佇列，用來判斷是否有位下載任務跟是否下載完成
    private var downloadInfoMap: MutableMap<Int, ArrayList<DownloadInfo>> = mutableMapOf()

    //下載任務資訊，用來作為刪除檔案、寄通知的依據
    private var downloadTaskInfoMap: MutableMap<Int, DownloadTaskInfo> = mutableMapOf()

    companion object {
        const val DOWNLOAD_STATUS_UPDATE = "download_status_update"
        const val DOWNLOAD_PROGRESS = "download_progress"
        const val DOWNLOAD_REQUEST_CLASS = "download_request_class"
        const val DOWNLOAD_TASK_INFO = "download_task_info"
    }

    override fun update(notificationId: Int, result: Int) {
        Log.d(TAG, "update: ")
        handleDownloadResult(notificationId, result)
        checkAllDownloadFinished(notificationId, result)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
//        return super.onStartCommand(intent, flags, startId)
        if (intent == null) return START_NOT_STICKY
        val downloadTaskInfo =
            (intent.getSerializableExtra(DOWNLOAD_TASK_INFO) as DownloadTaskInfo)
        if (downloadTaskInfo.downloadInfoList.isNullOrEmpty()) return START_NOT_STICKY
        val downloadTask =
            DownloadTask(this@DownloadService, downloadTaskInfo.downloadInfoList.first())
        downloadTask.registerObserver(this@DownloadService)
        startForeground(downloadTask.notificationId, downloadTask.notificationBuilder.build())
        downloadTaskInfoMap.put(downloadTask.notificationId, downloadTaskInfo)
        downloadInfoMap.put(downloadTask.notificationId, downloadTaskInfo.downloadInfoList)
        CoroutineScope(Dispatchers.IO).launch {
            //download start
            downloadTask.startDownload()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: ")
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
        downloadInfoMap.clear()
        downloadTaskInfoMap.clear()
    }

    private fun checkAllDownloadFinished(notificationId: Int, result: Int) {
        Log.d(TAG, "checkAllDownloadFinished: ")
        if (result != SUCCEED && result != FAILED) return
        downloadInfoMap[notificationId]!!.removeFirstOrNull()
        //佇列內還有未下載的項目
        if (downloadInfoMap[notificationId]!!.isNotEmpty()) {
            if (NetworkHandler.checkInternet(this)) {
                //有網路，下載下一個檔案
                CoroutineScope(Dispatchers.IO).launch {
                    val downloadTask =
                        DownloadTask(
                            this@DownloadService,
                            downloadInfoMap[notificationId]!!.first(),
                            notificationId
                        )
                    downloadTask.registerObserver(this@DownloadService)
                    startForeground(notificationId, downloadTask.notificationBuilder.build())
                    downloadTask.startDownload()
                }
            } else {
                //無網路，通知下載失敗並清空資料結束service
                update(notificationId, FAILED)
            }
        } else {
            //佇列下載完畢清空
            downloadInfoMap.remove(notificationId)
            if (downloadInfoMap.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    stopForeground(false)
                }
                stopSelf()
            }
        }
    }

    private fun handleDownloadResult(notificationId: Int, result: Int) {
        Log.d(TAG, "handleDownloadResult: ")
        if (result == FAILED) {
            //failed, 刪除下載的檔案跟資料夾
            deleteFiles(notificationId)
        }
        //寄出廣播
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            Intent(DOWNLOAD_STATUS_UPDATE).also {
                it.putExtra(
                    DOWNLOAD_REQUEST_CLASS,
                    downloadTaskInfoMap[notificationId]!!.requestClass
                )
                it.putExtra(DOWNLOAD_PROGRESS, result)
            }
        )
    }

    private fun deleteFiles(notificationId: Int) {
        downloadTaskInfoMap[notificationId]?.downloadInfoList?.forEach {
            File(it.filePath).also {
                it.delete()
                if (it.parentFile != null && it.parentFile.isDirectory && it.parentFile.listFiles()
                        .isEmpty()
                ) {
                    it.parentFile.delete()
                }
            }
        }
    }
}