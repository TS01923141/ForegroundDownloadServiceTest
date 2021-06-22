package com.example.foregrounddownloadservicetest.module

import androidx.versionedparcelable.ParcelField
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val DOWNLOAD_INFO_List = "download_info_list"
data class DownloadInfo(
    val filePath: String,
    val url: String,
    val upZip: Boolean = false
): Serializable