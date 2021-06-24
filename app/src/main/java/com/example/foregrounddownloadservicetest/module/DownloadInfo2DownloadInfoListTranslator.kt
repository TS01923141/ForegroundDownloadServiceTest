package com.example.foregrounddownloadservicetest.module

import java.lang.IllegalArgumentException
import java.util.ArrayList

object DownloadInfo2DownloadInfoListTranslator: DownloadInfoListTranslator() {
    override fun <T> translate(downloadData: T): ArrayList<DownloadInfo> {
        if (downloadData !is DownloadInfo) throw IllegalArgumentException("Not DownloadInfo")
        if (downloadData == DownloadInfo.empty) throw IllegalArgumentException("Empty DownloadInfo")
        downloadInfoList.add(downloadData)
        return downloadInfoList
    }
}