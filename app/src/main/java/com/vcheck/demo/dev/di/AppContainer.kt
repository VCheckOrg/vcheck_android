package com.vcheck.demo.dev.di

import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.data.ApiClient
import com.vcheck.demo.dev.data.LocalDatasource
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.RemoteDatasource
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class AppContainer(val app: VcheckDemoApp) {

    private var retrofit: Retrofit

    init {
        val logging = HttpLoggingInterceptor()

        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder().build()
            val hasMultipart: Boolean = request.headers.names().contains("multipart")
            logging.setLevel(if (hasMultipart) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY)
            chain.proceed(request)
        }.build()

        httpClient.addInterceptor(logging)
        httpClient.readTimeout(180, TimeUnit.SECONDS) //3min
        httpClient.connectTimeout(180, TimeUnit.SECONDS) //3min

        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .baseUrl(RemoteDatasource.API_BASE_URL) //TEST(DEV)
            .build()
    }

    private val remoteDataSource = RemoteDatasource(retrofit.create(ApiClient::class.java))

    private val localDatasource = LocalDatasource()

    val mainRepository = MainRepository(remoteDataSource, localDatasource)
}
