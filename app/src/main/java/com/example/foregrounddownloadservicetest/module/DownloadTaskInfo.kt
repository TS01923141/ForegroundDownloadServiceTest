package com.example.foregrounddownloadservicetest.module

import java.io.Serializable

data class DownloadTaskInfo(
    val downloadInfoList: ArrayList<DownloadInfo>,
    val requestClass: String
) : Serializable {
    companion object{
        val empty = DownloadTaskInfo(arrayListOf(), "")
    }
}