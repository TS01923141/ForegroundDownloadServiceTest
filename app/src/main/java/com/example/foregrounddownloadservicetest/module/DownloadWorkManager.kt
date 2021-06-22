package com.example.foregrounddownloadservicetest.module

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.foregrounddownloadservicetest.module.retrofit.DownloadFileRepository
import okhttp3.ResponseBody
import java.io.*

private const val TAG = "DownloadWorkManager"

class DownloadWorkManager(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private var fileSize: Long = 0
    private var downloadSize: Long = 0
    private var progress: Int = 0
    private lateinit var downloadInfo: DownloadInfo

    companion object {
        const val PROGRESS = "progress"
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork: ")
        setProgressAsync(Data.Builder().putInt(PROGRESS, progress).build())
        downloadInfo =
            Util.deserializeFromJson(inputData.getString(DOWNLOAD_INFO_List)) ?: return Result.failure()
        val response = DownloadFileRepository.downloadFile(downloadInfo.url)
        if (response != null) {
            if (writeResponseBodyToDisk(response)) {
                return Result.success()
            }
        }
        return Result.failure()
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        val downloadFile = File(downloadInfo.filePath)
        if (downloadFile.parentFile != null && !downloadFile.parentFile.exists()) {
            downloadFile.parentFile.mkdirs()
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val fileReader = ByteArray(4096)
            fileSize = body.contentLength()
            inputStream = body.byteStream()
            outputStream = FileOutputStream(downloadFile.absolutePath)
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                downloadSize += read.toLong()
//                Log.d(TAG, "writeResponseBodyToDisk: file download: $downloadSize of $fileSize")
//                Log.d(TAG, "writeResponseBodyToDisk: download percent: " + (1f * downloadSize / fileSize * 100))
                updateProgress()
            }
            outputStream.flush()
            downloadSize == fileSize
        } catch (e: IOException) {
            Log.e(TAG, "writeResponseBodyToDisk: e: " + e.message)
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()
            return true
        }
    }

    private fun updateProgress() {
        if (downloadSize >= fileSize * (progress + 1) / 100) {
            progress = (1f * downloadSize / fileSize * 100).toInt()
            Log.d(TAG, "updateProgress: progress: $progress")
            setProgressAsync(Data.Builder().putInt(PROGRESS, progress).build())
        }
    }
}