package com.example.foregrounddownloadservicetest.module

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.foregrounddownloadservicetest.module.DownloadService.Companion.DOWNLOAD_TASK_INFO
import java.lang.IllegalArgumentException

private const val TAG = "DownloadRepository"
object DownloadRepository {

    fun <T> downloadFile(context: Context, className: String, downloadData: T) {
            Intent(context, DownloadService::class.java)
                .also {
                    it.putExtra(DOWNLOAD_TASK_INFO, translateDownloadList(className, downloadData))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(it)
                    } else {
                        context.startService(it)
                    }
                }
    }

    private fun <T> translateDownloadList(className: String, downloadData: T): DownloadTaskInfo{
        var downloadTaskInfo = DownloadTaskInfo(arrayListOf(), className)
        //工廠
        when (downloadData) {
            is String -> {
                downloadTaskInfo.downloadInfoList.addAll(String2DownloadInfoListTranslator.translate(downloadData))
            }
            is List<*> -> {
                downloadTaskInfo.downloadInfoList.addAll(List2DownloadInfoListTranslator.translate(downloadData))
            }
            is DownloadInfo -> {
                downloadTaskInfo.downloadInfoList.addAll(DownloadInfo2DownloadInfoListTranslator.translate(downloadData))
            }
            else -> {
                throw IllegalArgumentException("Unknown type")
            }
        }
        return downloadTaskInfo
    }
}