package com.example.foregrounddownloadservicetest.module.retrofit

import com.example.foregrounddownloadservicetest.module.NetworkHandler
import okhttp3.ResponseBody
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

class DownloadFileRepository @Inject constructor(
    private val networkHandler: NetworkHandler,
    private val service: DownloadFileService) {
    fun downloadFile(fileUrl: String): ResponseBody? {
        if (!networkHandler.checkInternet()) return null
        return try {
            val response = service.downloadFile(fileUrl).execute()
            if (response.isSuccessful) response.body()
            else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}