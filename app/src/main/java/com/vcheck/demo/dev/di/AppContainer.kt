package com.vcheck.demo.dev.di

import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.data.ApiClient
import com.vcheck.demo.dev.data.LocalDatasource
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.RemoteDatasource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(val app: VcheckDemoApp) {

    private var retrofit: Retrofit

    init {
        val logging = HttpLoggingInterceptor()

        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor(logging)

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .baseUrl("https://test-verification.vycheck.com/api/") //TEST/DEV
            .build()
    }

    private val remoteDataSource = RemoteDatasource(retrofit.create(ApiClient::class.java))

    val localDatasource = LocalDatasource()

    val mainRepository = MainRepository(remoteDataSource)
}