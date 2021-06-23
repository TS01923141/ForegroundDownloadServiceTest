package com.example.foregrounddownloadservicetest

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.foregrounddownloadservicetest.databinding.ActivityMainBinding
import com.example.foregrounddownloadservicetest.module.DOWNLOAD_DIR_NAME
import com.example.foregrounddownloadservicetest.module.DownloadInfo
import com.example.foregrounddownloadservicetest.module.DownloadRepository
import com.example.foregrounddownloadservicetest.module.DownloadService
import java.io.File

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var downloadIdList : MutableList<Int> = arrayListOf()
    private val startDownloadReceiver = StartDownloadReceiver()
    private val downloadCompleteReceiver = DownloadCompleteReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //--

        LocalBroadcastManager.getInstance(this).registerReceiver(
            startDownloadReceiver,
            IntentFilter(DownloadService.DOWNLOAD_START))

        LocalBroadcastManager.getInstance(this).registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadService.DOWNLOAD_COMPLETE))

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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(startDownloadReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadCompleteReceiver)
        super.onDestroy()
    }

    private inner class StartDownloadReceiver(): BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "StartDownloadReceiver onReceive: ")
            if (intent == null) return
            val className = intent.getStringExtra(DownloadService.DOWNLOAD_REQUEST_CLASS)
            Log.d(TAG, "onReceive: className: $className")
            Log.d(TAG, "onReceive: this.javaClass.name: " + this@MainActivity.javaClass.name)
            Log.d(TAG, "onReceive: this.className == receiver.className: " + (className == this@MainActivity.javaClass.name))
            if (!className.isNullOrEmpty() && className == this@MainActivity.javaClass.name){
                downloadIdList.add(intent.getIntExtra(DownloadService.DOWNLOAD_TASK_NOTIFICATION_ID , -1))
                Log.d(TAG, "onReceive: downloadId: $downloadIdList")
            }
        }
    }

    private inner class DownloadCompleteReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "DownloadResultReceiver onReceive: ")
            if (intent == null) return
            val downloadCompleteId = intent.getIntExtra(DownloadService.DOWNLOAD_TASK_NOTIFICATION_ID, -1)
            Log.d(TAG, "onReceive: downloadCompleteId: $downloadCompleteId")
            if (downloadCompleteId != -1) {
                for (downloadId in downloadIdList) {
                    Log.d(TAG, "onReceive: downloadId: " + downloadId)
                    Log.d(TAG, "onReceive: (downloadCompleteId == downloadId): " + (downloadCompleteId == downloadId))
                    if (downloadCompleteId == downloadId) {
                        val result = intent.getStringExtra(DownloadService.DOWNLOAD_RESULT)
                        Log.d(TAG, "onReceive: downloadCompleted: $downloadCompleteId, result: $result")
                        binding.textViewMain.text = result
                        downloadIdList.remove(downloadId)
                        break
                    }
                }
            }
        }
    }
}