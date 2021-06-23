package com.example.foregrounddownloadservicetest.module

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.foregrounddownloadservicetest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

private const val TAG = "DownloadService"

class DownloadService : Service(), Observer {
    private var _downloadInfoMap: MutableMap<Int, ArrayList<DownloadInfo>> = mutableMapOf()
    private var downloadInfoMap: MutableMap<Int, ArrayList<DownloadInfo>> = mutableMapOf()

    companion object {
        const val DOWNLOAD_START = "download_start"
        const val DOWNLOAD_COMPLETE = "download_complete"
        const val DOWNLOAD_RESULT = "download_result"
        const val DOWNLOAD_TASK_NOTIFICATION_ID = "download_task_notificaion_id"
        const val DOWNLOAD_REQUEST_CLASS = "download_request_class"
        const val DOWNLOAD_INFO_LIST = "download_info_list"
    }

    override fun update(notificationId: Int, result: String) {
        Log.d(TAG, "update: ")
        checkAllDownloadFinished(notificationId, result)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
//        return super.onStartCommand(intent, flags, startId)
        if (intent == null) return START_NOT_STICKY
        val downloadInfoList =
            (intent.getSerializableExtra(DOWNLOAD_INFO_LIST) as ArrayList<DownloadInfo>)
        if (downloadInfoList.isNullOrEmpty()) return START_NOT_STICKY
        CoroutineScope(Dispatchers.IO).launch {
            val downloadTask =
                DownloadTask(this@DownloadService, downloadInfoList.first())
            downloadTask.registerObserver(this@DownloadService)
            startForeground(downloadTask.notificationId, downloadTask.notificationBuilder.build())
            _downloadInfoMap.put(downloadTask.notificationId, downloadInfoList)
            downloadInfoMap.put(downloadTask.notificationId, downloadInfoList)
            //send start download task broadcast
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                Intent(DOWNLOAD_START).also {
                    it.putExtra(
                        DOWNLOAD_REQUEST_CLASS,
                        intent.getStringExtra(DOWNLOAD_REQUEST_CLASS)
                    )
                    it.putExtra(DOWNLOAD_TASK_NOTIFICATION_ID, downloadTask.notificationId)
                })
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
    }

    private fun checkAllDownloadFinished(notificationId: Int, result: String) {
        Log.d(TAG, "checkAllDownloadFinished: ")
        //
        if (result == getString(R.string.download_failed)) {
            //failed, 刪除下載的檔案跟資料夾
            deleteFiles(notificationId)
            if (!NetworkHandler.checkInternet(this)) {
                //無網路，通知下載結束
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                    Intent(DOWNLOAD_COMPLETE).also {
                        it.putExtra(DOWNLOAD_TASK_NOTIFICATION_ID, notificationId)
                        it.putExtra(DOWNLOAD_RESULT, getString(R.string.download_failed))
                    }
                )
                return
            }
        }

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
            //通知下載結束
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                Intent(DOWNLOAD_COMPLETE).also {
                    it.putExtra(DOWNLOAD_TASK_NOTIFICATION_ID, notificationId)
                    it.putExtra(DOWNLOAD_RESULT, result)
                }
            )
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

    private fun deleteFiles(notificationId: Int) {
        _downloadInfoMap[notificationId]?.forEach {
            File(it.filePath).also {
                it.delete()
                if (it.parentFile != null && it.parentFile.isDirectory && it.parentFile.listFiles().isEmpty()){
                    it.parentFile.delete()
                }
            }
        }
    }
}