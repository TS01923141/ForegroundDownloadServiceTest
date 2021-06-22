package com.example.foregrounddownloadservicetest.module

import com.google.gson.Gson

object Util {
    fun serializeToJson(myClass: DownloadInfo?): String? {
        val gson = Gson()
        return gson.toJson(myClass)
    }

    // Deserialize to single object.
    fun deserializeFromJson(jsonString: String?): DownloadInfo? {
        val gson = Gson()
        return gson.fromJson(jsonString, DownloadInfo::class.java)
    }
}