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
                it.putExtra(DOWNLOAD_INFO_List, translateDownloadList(context, downloadData))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                } else {
                    context.startService(it)
                }
            }
    }

    private fun <T> translateDownloadList(context: Context, downloadData: T): ArrayList<DownloadInfo>{
        val downloadInfoList: ArrayList<DownloadInfo> = arrayListOf()
        //簡單工廠
        when (downloadData) {
            is String -> {
                val cw = ContextWrapper(context)
                val directory = cw.getDir(DOWNLOAD_DIR_NAME, AppCompatActivity.MODE_PRIVATE)
                if (!downloadData.contains("/")) throw IllegalArgumentException("Not Complete Url")
                val fileName = downloadData.split("/").last()
                val downloadInfo = DownloadInfo(
                    File(directory, fileName).absolutePath,
                    downloadData
                )
                downloadInfoList.add(downloadInfo)
            }
            is List<*> -> {
                for (string in downloadData){
                    if (string !is String) throw IllegalArgumentException("List<String> Only")

                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(DOWNLOAD_DIR_NAME, AppCompatActivity.MODE_PRIVATE)
                    val fileName = string.split("/").last()
                    val downloadInfo = DownloadInfo(
                        File(directory, fileName).absolutePath,
                        string
                    )
                    downloadInfoList.add(downloadInfo)
                }
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