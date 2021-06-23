package com.example.foregrounddownloadservicetest.module

import androidx.versionedparcelable.ParcelField
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DownloadInfo(
    val filePath: String,
    val url: String,
    val upZip: Boolean = false
): Serializable {
    companion object {
        val empty = DownloadInfo("","")
    }
}