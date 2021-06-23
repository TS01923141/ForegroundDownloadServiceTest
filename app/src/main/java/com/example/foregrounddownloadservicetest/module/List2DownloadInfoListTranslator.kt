package com.example.foregrounddownloadservicetest.module

import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import com.example.foregrounddownloadservicetest.AndroidApplication
import java.io.File
import java.lang.IllegalArgumentException
import java.util.ArrayList

object List2DownloadInfoListTranslator : DownloadInfoListTranslator() {
    override fun <T> translate(downloadData: T): ArrayList<DownloadInfo> {
        if (downloadData !is List<*>) throw IllegalArgumentException("Not List")
        for (string in downloadData){
            if (string !is String) throw IllegalArgumentException("List<String> Only")

            val context = AndroidApplication.instance
            val cw = ContextWrapper(context)
            val directory = cw.getDir(DOWNLOAD_DIR_NAME, AppCompatActivity.MODE_PRIVATE)
            val fileName = string.split("/").last()
            val downloadInfo = DownloadInfo(
                File(directory, fileName).absolutePath,
                string
            )
            downloadInfoList.add(downloadInfo)
        }
        return downloadInfoList
    }
}