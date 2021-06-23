package com.example.foregrounddownloadservicetest.module

interface Subject {
    fun registerObserver(observer: Observer)
    fun removeObserver(observer: Observer)
    fun notifyObservers(notificationId: Int, result: String)
}