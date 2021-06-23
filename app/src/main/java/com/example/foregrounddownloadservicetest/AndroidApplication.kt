package com.example.foregrounddownloadservicetest

import android.app.Application

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