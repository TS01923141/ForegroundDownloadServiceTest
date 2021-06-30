package com.example.foregrounddownloadservicetest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AndroidApplication : Application() {
    companion object {
        lateinit var instance: AndroidApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}