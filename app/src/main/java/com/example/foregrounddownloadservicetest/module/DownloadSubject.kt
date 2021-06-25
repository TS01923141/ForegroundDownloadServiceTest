package com.example.foregrounddownloadservicetest.module

interface DownloadSubject {
    fun registerObserver(observer: DownloadObserver)
    fun removeObserver(observer: DownloadObserver)
    fun notifyObservers(notificationId: Int, result: Int)
}