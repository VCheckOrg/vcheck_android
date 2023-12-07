package com.vcheck.sdk.core.di

import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.*
import com.vcheck.sdk.core.domain.VCheckEnvironment
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object VCheckDIContainer {

    private var verificationRetrofit: Retrofit

    init {
        verificationRetrofit = getVerifApiRetrofit()
    }

    private fun getHttpClient(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor()

        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder().build()
            //logging.setLevel(HttpLoggingInterceptor.Level.NONE)
            val hasMultipart: Boolean = request.headers.names().contains("multipart")
            logging.setLevel(if (hasMultipart) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY)
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            chain.proceed(request)
        }.build()

        httpClient.addInterceptor(logging)
        httpClient.readTimeout(180, TimeUnit.SECONDS) //3min
        httpClient.connectTimeout(180, TimeUnit.SECONDS) //3min

        return httpClient
    }

    private fun getVerifApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(getHttpClient().build())
            .baseUrl(
                if (VCheckSDK.getEnvironment() == VCheckEnvironment.DEV)
                    VCheckSDKConstantsProvider.DEV_VERIFICATIONS_API_BASE_URL
                else VCheckSDKConstantsProvider.PARTNER_VERIFICATIONS_API_BASE_URL)
            .build()
    }

    private val remoteDataSource = RemoteDatasource(
        verificationRetrofit.create(VerificationApiClient::class.java))

    private val localDatasource = LocalDatasource()

    val mainRepository = MainRepository(remoteDataSource, localDatasource)

}
