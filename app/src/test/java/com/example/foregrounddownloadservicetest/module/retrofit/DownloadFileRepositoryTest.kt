package com.example.foregrounddownloadservicetest.module.retrofit

import android.content.Context
import com.example.foregrounddownloadservicetest.UnitTest
import com.example.foregrounddownloadservicetest.module.DownloadRepository.downloadFile
import com.example.foregrounddownloadservicetest.module.NetworkHandler
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runners.JUnit4
import retrofit2.Call
import retrofit2.Response

class DownloadFileRepositoryTest: UnitTest() {

    private lateinit var repository: DownloadFileRepository

    @MockK private lateinit var networkHandler: NetworkHandler
    @MockK private lateinit var response : Response<ResponseBody>
    @MockK private lateinit var call: Call<ResponseBody>
    @MockK private lateinit var service: DownloadFileService

    @Before fun setUp(){
        repository = DownloadFileRepository(networkHandler, service)
    }

    @Test
    fun `should return null when no network`(){
        every{ networkHandler.checkInternet() } returns false
        val response = repository.downloadFile("")

        assertEquals(response, null)
        verify { repository.downloadFile("") }
    }

    @Test
    fun `should return null if service if no successful response`(){
        every { networkHandler.checkInternet() } returns true
        every { response.isSuccessful } returns false
        every { response.body() } returns null
        every { call.execute() } returns response
        every { service.downloadFile("") } returns call

        val response = repository.downloadFile("")
        assertEquals(response , null)
    }
}