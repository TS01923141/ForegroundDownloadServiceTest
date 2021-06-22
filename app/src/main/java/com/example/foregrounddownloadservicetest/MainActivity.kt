package com.example.foregrounddownloadservicetest

import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.foregrounddownloadservicetest.module.DOWNLOAD_INFO_List
import com.example.foregrounddownloadservicetest.module.DownloadInfo
import com.example.foregrounddownloadservicetest.module.DownloadService
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cw = ContextWrapper(this)
        val directory = cw.getDir("downloadFiles", MODE_PRIVATE)
        val downloadInfo = DownloadInfo(
            File(directory, "MOI_OSM_Taiwan_TOPO_Rudy.map.zip").absolutePath,
            "http://moi.kcwu.csie.org/MOI_OSM_Taiwan_TOPO_Rudy.map.zip"
        )
        val downloadInfoList : ArrayList<DownloadInfo> = arrayListOf()
        downloadInfoList.add(downloadInfo)
//        Intent(this, DownloadService::class.java)
//            .also {
//                it.putExtra(DOWNLOAD_INFO, downloadInfo)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(it)
//                }else{
//                    startService(it)
//                }
//            }

        //--

        val downloadInfo2 = DownloadInfo(
            File(directory, "02d@2x.png").absolutePath,
            "http://openweathermap.org/img/wn/02d@2x.png"
        )
        downloadInfoList.add(downloadInfo2)
        Intent(this, DownloadService::class.java)
            .also {
//                it.putExtra(DOWNLOAD_INFO_List, downloadInfo2)
                it.putExtra(DOWNLOAD_INFO_List, downloadInfoList)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                }else{
                    startService(it)
                }
            }
        val downloadInfoList2 : ArrayList<DownloadInfo> = arrayListOf()
        downloadInfoList2.add(downloadInfo2)
        Intent(this, DownloadService::class.java)
            .also {
                it.putExtra(DOWNLOAD_INFO_List, downloadInfoList2)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                }else{
                    startService(it)
                }
            }
    }
}