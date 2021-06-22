package com.example.foregrounddownloadservicetest.module

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

private const val TAG = "DownloadService"

class DownloadService : Service(), Observer {
    private var downloadInfoMap: MutableMap<Int, ArrayList<DownloadInfo>> = mutableMapOf()

//    companion object {
//        const val PROGRESS = "progress"
//    }

    override fun update(notificationId: Int) {
        Log.d(TAG, "update: ")
        checkAllDownloadFinished(notificationId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
//        return super.onStartCommand(intent, flags, startId)
        if (intent == null) return START_NOT_STICKY
        val downloadInfoList =
            (intent.getSerializableExtra(DOWNLOAD_INFO_List) as ArrayList<DownloadInfo>)
        if (downloadInfoList.isNullOrEmpty()) return START_NOT_STICKY
        CoroutineScope(Dispatchers.IO).launch {
            val downloadTask =
                DownloadTask(this@DownloadService, downloadInfoList.first())
            downloadTask.registerObserver(this@DownloadService)
            startForeground(downloadTask.notificationId, downloadTask.notificationBuilder.build())
            downloadInfoMap.put(downloadTask.notificationId, downloadInfoList)
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
    }

    private fun checkAllDownloadFinished(notificationId: Int) {
        Log.d(TAG, "checkAllDownloadFinished: ")
        downloadInfoMap[notificationId]!!.removeFirstOrNull()
        //佇列內還有未下載的項目
        if (downloadInfoMap[notificationId]!!.isNotEmpty()) {
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
}