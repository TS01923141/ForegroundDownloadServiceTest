package com.example.foregrounddownloadservicetest.module.retrofit

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://openweathermap.org/"
object DownloadFileRetrofit{

    var okHttpClient = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
//        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .retryOnConnectionFailure(false)
        .dispatcher(initDispatcher())
        .build()

    /**自定義Dispatcher, 修改最大需求數, 避免time out
     * @return Dispatcher
     */
    private fun initDispatcher(): Dispatcher {
        val dispatcher = Dispatcher()
        //最多需求數
        dispatcher.maxRequests = 64
        //最多同時執行的需求
        dispatcher.maxRequestsPerHost = 10
        return dispatcher
    }

    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }
}