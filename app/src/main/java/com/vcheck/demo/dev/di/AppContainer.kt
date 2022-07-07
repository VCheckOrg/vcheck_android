package com.vcheck.demo.dev.di

import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.data.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


internal class AppContainer(val app: VCheckSDKApp) {

    private var verificationRetrofit: Retrofit

    private var partnerRetrofit: Retrofit

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

        verificationRetrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .baseUrl(RemoteDatasource.VERIFICATIONS_API_BASE_URL) //TEST(DEV)
            .build()

        partnerRetrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .baseUrl(RemoteDatasource.PARTNER_API_BASE_URL) //TEST(DEV)
            .build()
    }

    private val remoteDataSource = RemoteDatasource(
        verificationRetrofit.create(VerificationApiClient::class.java),
        partnerRetrofit.create(PartnerApiClient::class.java))

    private val localDatasource = LocalDatasource()

    val mainRepository = MainRepository(remoteDataSource, localDatasource)
}
