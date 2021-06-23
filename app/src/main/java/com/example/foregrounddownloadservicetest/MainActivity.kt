package com.example.foregrounddownloadservicetest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.foregrounddownloadservicetest.module.DownloadRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list : ArrayList<String> = arrayListOf()
        list.add("http://moi.kcwu.csie.org/MOI_OSM_Taiwan_TOPO_Rudy.map.zip")
        list.add("http://openweathermap.org/img/wn/02d@2x.png")
        DownloadRepository.downloadFile(this, list)
        DownloadRepository.downloadFile(this, "http://moi.kcwu.csie.org/MOI_OSM_Taiwan_TOPO_Rudy.map.zip")
    }
}