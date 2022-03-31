package com.vcheck.demo.di

import com.vcheck.demo.data.ApiClient
import com.vcheck.demo.data.MainRepository
import com.vcheck.demo.data.RemoteDatasource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer {

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

    val mainRepository = MainRepository(remoteDataSource)
}
