package com.example.foregrounddownloadservicetest.module

import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import com.example.foregrounddownloadservicetest.AndroidApplication
import java.io.File
import java.lang.IllegalArgumentException
import java.util.ArrayList

object String2DownloadInfoListTranslator: DownloadInfoListTranslator() {
    override fun <T> translate(downloadData: T): ArrayList<DownloadInfo> {
        if (downloadData !is String) throw IllegalArgumentException("Not String")
        val context = AndroidApplication.instance
        val cw = ContextWrapper(context)
        val directory = cw.getDir(DOWNLOAD_DIR_NAME, AppCompatActivity.MODE_PRIVATE)
        if (!downloadData.contains("/")) throw IllegalArgumentException("Not Complete Url")
        val fileName = downloadData.split("/").last()
        val downloadInfo = DownloadInfo(
            File(directory, fileName).absolutePath,
            downloadData
        )
        downloadInfoList.add(downloadInfo)
        return downloadInfoList
    }
}