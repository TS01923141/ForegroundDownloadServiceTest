package com.example.foregrounddownloadservicetest.module.retrofit

import okhttp3.ResponseBody
import java.lang.Exception

object DownloadFileRepository {
    fun downloadFile(fileUrl: String): ResponseBody? {
        return try {
            val service = DownloadFileRetrofit.provideRetrofit(DownloadFileRetrofit.provideOkHttpClient())
                .create(DownloadFileService::class.java)
            val response = service.downloadFile(fileUrl).execute()
            if (response.isSuccessful) response.body()
            else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}