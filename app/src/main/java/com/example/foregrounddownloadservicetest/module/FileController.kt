package com.example.foregrounddownloadservicetest.module

import android.util.Log
import okhttp3.ResponseBody
import java.io.*
import java.net.SocketException
import java.net.URL
import java.util.zip.ZipInputStream

private const val TAG = "FileController"
object FileController {

    //從url取得檔案大小
    @Throws(IOException::class)
    fun getUrlFileSize(fileUrl: String?): Int {
        val url = URL(fileUrl)
        val urlConnection = url.openConnection()
        urlConnection.setRequestProperty("Accept-Encoding", "identity")
        urlConnection.connect()
        return urlConnection.contentLength
    }

    @Throws(IOException::class)
    fun compareFileSizeWithUrl(filePath: String?, fileUrl: String?): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false
        val urlFileSize = getUrlFileSize(fileUrl)
        //有時候回傳的大小會跟下載檔案有些許誤差，目前會給予5%的誤差空間
        return urlFileSize * 1.05 >= file.length() && urlFileSize * 0.95 <= file.length()
    }

    fun writeResponseBodyToDisk(fileWriteObserver: FileWriteObserver?, filePath: String, body: ResponseBody): Boolean {
        var downloadProgress = 0
        var downloadSize: Long = 0
        val downloadFile = File(filePath)
        if (downloadFile.parentFile != null && !downloadFile.parentFile.exists()) {
            downloadFile.parentFile.mkdirs()
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val fileReader = ByteArray(4096)
            val fileSize = body.contentLength()
            inputStream = body.byteStream()
            outputStream = FileOutputStream(downloadFile.absolutePath)
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                downloadSize += read.toLong()
                if (fileWriteObserver != null && downloadSize >= fileSize * (downloadProgress + 1) / 100) {
                    downloadProgress = calculateProgress(downloadSize, fileSize)
                    fileWriteObserver.updateFileWriteProgress(downloadProgress)
                }
            }
            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: SocketException) {
            e.printStackTrace()
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()
            return true
        }
    }

    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString ZIP的名称
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    fun UnZipFolder(zipFileString: String, outPathString: String) {
        Log.d(TAG, "UnZipFolder: zipFileString: $zipFileString")
        var inZip = ZipInputStream(FileInputStream(zipFileString))
        var zipEntry = inZip.nextEntry
        var szName = ""
        while (zipEntry != null) {
            szName = zipEntry.name
            if (zipEntry.isDirectory) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length - 1)
                val folder = File(outPathString + File.separator + szName)
                folder.mkdirs()
            } else {
                val file = File(outPathString + File.separator + szName)
                if (!file.exists()) {
                    file.parentFile.mkdirs()
                    file.createNewFile()
                }
                // 获取文件的输出流
                val out = FileOutputStream(file)
                var len: Int
                val buffer = ByteArray(1024)
                // 读取（字节）字节到缓冲区
                while (inZip.read(buffer).also { len = it } != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len)
                    out.flush()
                }
                out.close()
            }
            zipEntry = inZip.nextEntry
        }
        inZip.close()
    }

    private fun calculateProgress(downloadSize: Long, fileSize: Long): Int{
        return (1f * downloadSize / fileSize * 100).toInt()
    }
}