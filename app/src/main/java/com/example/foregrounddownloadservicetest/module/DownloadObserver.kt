package com.example.foregrounddownloadservicetest.module

interface DownloadObserver {
    fun update(notificationId: Int, result: Int)
}