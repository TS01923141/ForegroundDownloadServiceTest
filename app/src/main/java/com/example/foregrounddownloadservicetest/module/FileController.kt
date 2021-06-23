package com.example.foregrounddownloadservicetest.module

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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

    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString ZIP的名称
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    fun UnZipFolder(zipFileString: String, outPathString: String) {
        Log.d(TAG, "UnZipFolder: zipFileString: $zipFileString")
//        var totalZipCount = 0
//        var currentZipCount = 0
        var inZip = ZipInputStream(FileInputStream(zipFileString))
//        while (inZip.nextEntry != null) {
//            totalZipCount++
//        }
//        inZip = ZipInputStream(FileInputStream(zipFileString))
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
//                currentZipCount++
//                val unZipProgressPercent =
//                    (currentZipCount.toDouble() / totalZipCount * 100).toInt()
//                Log.d(TAG, "UnZipFolder: unZip progress: $unZipProgressPercent")
            }
            zipEntry = inZip.nextEntry
        }
        inZip.close()
    }
}