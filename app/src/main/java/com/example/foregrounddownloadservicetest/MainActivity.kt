package com.example.foregrounddownloadservicetest

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.foregrounddownloadservicetest.databinding.ActivityMainBinding
import com.example.foregrounddownloadservicetest.module.*
import java.io.File

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val downloadCompleteReceiver = DownloadStatusUpdateReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //--

        LocalBroadcastManager.getInstance(this).registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadService.DOWNLOAD_STATUS_UPDATE)
        )

        //--test1

        val list: ArrayList<String> = arrayListOf()
//        list.add("http://moi.kcwu.csie.org/MOI_OSM_Taiwan_TOPO_Rudy.map.zip")
        list.add("http://openweathermap.org/img/wn/02d@2x.png")
        DownloadRepository.downloadFile(this, javaClass.name, list)

        //--test2

        val context = AndroidApplication.instance
        val cw = ContextWrapper(context)
        val directory = cw.getDir(DOWNLOAD_DIR_NAME, AppCompatActivity.MODE_PRIVATE)
        val downloadInfo = DownloadInfo(
            File(directory, "MOI_OSM_Taiwan_TOPO_Rudy.zip").absolutePath,
            "http://moi.kcwu.csie.org/MOI_OSM_Taiwan_TOPO_Rudy.map.zip", true
        )
        DownloadRepository.downloadFile(this, javaClass.name, downloadInfo)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadCompleteReceiver)
        super.onDestroy()
    }

    private inner class DownloadStatusUpdateReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "DownloadResultReceiver onReceive: ")
            if (intent == null) return
            val requestClass = intent.getStringExtra(DownloadService.DOWNLOAD_REQUEST_CLASS)
            if (requestClass == this@MainActivity.javaClass.name) {
                val resultText =
                    when (val result = intent.getIntExtra(DownloadService.DOWNLOAD_PROGRESS, 0)) {
                        FAILED ->
                            getString(R.string.download_failed)
                        SUCCEED ->
                            getString(R.string.download_succeed)
                        UNZIP ->
                            getString(R.string.file_unzipping)
                        else ->
                            "progress: $result"
                    }
                binding.textViewMain.text = resultText
            }
        }
    }
}