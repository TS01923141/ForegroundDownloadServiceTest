package com.example.foregrounddownloadservicetest.module

interface FileWriteObserver {
    fun updateFileWriteProgress(progress: Int)
}