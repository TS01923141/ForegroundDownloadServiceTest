package com.example.foregrounddownloadservicetest

import com.example.foregrounddownloadservicetest.module.retrofit.DownloadFileRepository
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TAG = "ApplicationModule"
@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    private val BASE_URL = "http://openweathermap.org/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    private fun createClient(): OkHttpClient {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .retryOnConnectionFailure(false)
            .dispatcher(initDispatcher())
        return okHttpClientBuilder.build()
    }

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

//    @Provides
//    @Singleton
//    fun provideDownloadFileRepository(dataSource: DownloadFileRepository.Network): DownloadFileRepository = dataSource
}