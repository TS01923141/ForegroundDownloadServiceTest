package com.example.foregrounddownloadservicetest.module

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foregrounddownloadservicetest.R
import com.example.foregrounddownloadservicetest.UnitTest
import com.example.foregrounddownloadservicetest.module.retrofit.DownloadFileRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.IllegalArgumentException

private const val TAG = "DownloadTaskTest"

class DownloadTaskTest : UnitTest() {
    private val downloadInfo = DownloadInfo("/data/DownloadTaskTest/test.zip", "http://DownloadTaskTest/test.zip")
    private lateinit var downloadTask: DownloadTask
    private val notificationId = 1

    @MockK
    val context: Context = mockk(relaxed = true)

    @MockK
    val notificationManager: NotificationManager = mockk()

    @MockK
    val notificationBuilder: NotificationCompat.Builder = mockk()

    @MockK
    val observer: DownloadObserver = mockk()

    val responseBody: ResponseBody = ResponseBody.create("application/octet-stream; charset=utf-8".toMediaType(), "")

    @MockK
    lateinit var downloadFileRepository: DownloadFileRepository


    @Before
    fun setUp() {
        every { notificationBuilder.build() } returns Notification()
        every { notificationBuilder.setContentText(any()) } returns notificationBuilder
        every { notificationBuilder.setProgress(any(), any(), any()) } returns notificationBuilder
        every { notificationBuilder.setAutoCancel(any()) } returns notificationBuilder
        every { notificationBuilder.setOngoing(any()) } returns notificationBuilder

        every { context.getString(any()) } returns ""
        every {
            notificationManager.notify(
                notificationId,
                notificationBuilder.build()
            )
        } returns Unit
        every { notificationManager.activeNotifications } returns emptyArray()
        every { context.getSystemService(Service.NOTIFICATION_SERVICE) } returns notificationManager
        downloadTask = DownloadTask(context, downloadInfo, notificationId)
        downloadTask.notificationBuilder = notificationBuilder
        downloadTask.downloadFileRepository = downloadFileRepository
    }

    @Test
    fun `startDownload should display FAILED if response is null`() = runBlocking {
        every { observer.update(notificationId, FAILED) } returns Unit
        every { downloadFileRepository.downloadFile(downloadInfo.url) } returns null

        downloadTask.registerObserver(observer)
        downloadTask.startDownload()
        verify { observer.update(notificationId, FAILED) }
    }

    @Test
    fun `startDownload should display FAILED if response writeResponseBodyToDisk is failure`() = runBlocking {
        every { observer.update(notificationId, FAILED) } returns Unit
        every { downloadFileRepository.downloadFile(downloadInfo.url) } returns responseBody
        mockkObject(FileController)
        every { FileController.writeResponseBodyToDisk(downloadTask, downloadInfo.filePath, responseBody) } returns false

        downloadTask.registerObserver(observer)
        downloadTask.startDownload()
        verify { observer.update(notificationId, FAILED) }
    }

    @Test
    fun `startDownload should display FAILED if response compareFileSizeWithUrl is failure`() = runBlocking {
        every { observer.update(notificationId, FAILED) } returns Unit
        every { downloadFileRepository.downloadFile(downloadInfo.url) } returns responseBody
        mockkObject(FileController)
        every { FileController.writeResponseBodyToDisk(downloadTask, downloadInfo.filePath, responseBody) } returns true
        every { FileController.compareFileSizeWithUrl(downloadInfo.filePath, downloadInfo.url) } returns false

        downloadTask.registerObserver(observer)
        downloadTask.startDownload()
        verify { observer.update(notificationId, FAILED) }
    }

    @Test
    fun `startDownload should display FAILED if response upZip is failure`() = runBlocking {
        val downloadInfo = DownloadInfo("/data/DownloadTaskTest/test.zip", "http://DownloadTaskTest/test.zip" , true)
        every { observer.update(notificationId, FAILED) } returns Unit
        every { downloadFileRepository.downloadFile(downloadInfo.url) } returns responseBody
        mockkObject(FileController)
        every { FileController.writeResponseBodyToDisk(downloadTask, downloadInfo.filePath, responseBody) } returns true
        every { FileController.compareFileSizeWithUrl(downloadInfo.filePath, downloadInfo.url) } returns true
        every { FileController.unZipFolder(downloadInfo.filePath, File(downloadInfo.filePath).parent!!) } throws IllegalArgumentException()

        downloadTask.registerObserver(observer)
        downloadTask.startDownload()
        verify { observer.update(notificationId, FAILED) }
    }

    @Test
    fun `startDownload should successful with no unzip`() = runBlocking {
        every { observer.update(notificationId, SUCCEED) } returns Unit
        every { downloadFileRepository.downloadFile(downloadInfo.url) } returns responseBody
        mockkObject(FileController)
        every { FileController.writeResponseBodyToDisk(downloadTask, downloadInfo.filePath, responseBody) } returns true
        every { FileController.compareFileSizeWithUrl(downloadInfo.filePath, downloadInfo.url) } returns true

        downloadTask.registerObserver(observer)
        downloadTask.updateFileWriteProgress(SUCCEED)
        downloadTask.startDownload()
        verify { observer.update(notificationId, SUCCEED) }
    }

    @Test
    fun `startDownload should successful with zip`() = runBlocking {
        val downloadInfo = DownloadInfo("/data/DownloadTaskTest/test.zip", "http://DownloadTaskTest/test.zip" , true)
        every { observer.update(notificationId, SUCCEED) } returns Unit
        every { downloadFileRepository.downloadFile(downloadInfo.url) } returns responseBody
        mockkObject(FileController)
        every { FileController.writeResponseBodyToDisk(downloadTask, downloadInfo.filePath, responseBody) } returns true
        every { FileController.compareFileSizeWithUrl(downloadInfo.filePath, downloadInfo.url) } returns true
        every { FileController.unZipFolder(downloadInfo.filePath, File(downloadInfo.filePath).parent!!) } returns Unit

        downloadTask.registerObserver(observer)
        downloadTask.updateFileWriteProgress(SUCCEED)
        downloadTask.startDownload()
        verify(exactly = 1) { observer.update(notificationId, SUCCEED) }
    }
}