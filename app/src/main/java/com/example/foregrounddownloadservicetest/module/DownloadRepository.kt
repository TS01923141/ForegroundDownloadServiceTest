package com.example.foregrounddownloadservicetest.module

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

object DownloadRepository {

    fun <T> downloadFile(context: Context, downloadData: T) {
        Intent(context, DownloadService::class.java)
            .also {
                it.putExtra(DOWNLOAD_INFO_List, translateDownloadList(downloadData))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                } else {
                    context.startService(it)
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