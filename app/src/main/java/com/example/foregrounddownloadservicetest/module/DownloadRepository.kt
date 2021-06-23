package com.example.foregrounddownloadservicetest.module

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.foregrounddownloadservicetest.module.DownloadService.Companion.DOWNLOAD_INFO_LIST
import com.example.foregrounddownloadservicetest.module.DownloadService.Companion.DOWNLOAD_REQUEST_CLASS
import java.lang.IllegalArgumentException
import java.util.*

object DownloadRepository {

    fun <T> downloadFile(context: Context, className: String, downloadData: T) {
        if (NetworkHandler.checkInternet(context)) {
            Intent(context, DownloadService::class.java)
                .also {
                    it.putExtra(DOWNLOAD_INFO_LIST, translateDownloadList(downloadData))
                    it.putExtra(DOWNLOAD_REQUEST_CLASS, className)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(it)
                    } else {
                        context.startService(it)
                    }
                }
        }
    }

    private fun <T> translateDownloadList(downloadData: T): ArrayList<DownloadInfo>{
        var downloadInfoList: ArrayList<DownloadInfo> = arrayListOf()
        //工廠
        when (downloadData) {
            is String -> {
                downloadInfoList = String2DownloadInfoListTranslator.translate(downloadData)
            }
            is List<*> -> {
                downloadInfoList = List2DownloadInfoListTranslator.translate(downloadData)
            }
            is DownloadInfo -> {
                if (downloadData == DownloadInfo.empty) throw IllegalArgumentException("Empty DownloadInfo")
                downloadInfoList.add(downloadData)
            }
            else -> {
                throw IllegalArgumentException("Unknown type")
            }
        }
        return downloadInfoList
    }
}