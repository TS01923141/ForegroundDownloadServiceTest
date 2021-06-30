package com.example.foregrounddownloadservicetest.module.retrofit

import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadFileService
@Inject constructor(retrofit: Retrofit) : DownloadFileApi{
    private val downloadFileApi by lazy { retrofit.create(DownloadFileApi::class.java) }

    override fun downloadFile(fileUrl: String) = downloadFileApi.downloadFile(fileUrl)
}