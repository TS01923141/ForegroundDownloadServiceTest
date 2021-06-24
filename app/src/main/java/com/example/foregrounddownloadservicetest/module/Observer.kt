package com.example.foregrounddownloadservicetest.module

interface Observer {
    fun update(notificationId: Int, result: Int)
}