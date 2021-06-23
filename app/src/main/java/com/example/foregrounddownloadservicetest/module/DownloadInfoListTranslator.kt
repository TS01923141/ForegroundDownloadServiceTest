package com.example.foregrounddownloadservicetest.module

import java.util.ArrayList

abstract class DownloadInfoListTranslator {
    protected val downloadInfoList: ArrayList<DownloadInfo> = arrayListOf()
    abstract fun <T> translate(downloadData: T): ArrayList<DownloadInfo>
}