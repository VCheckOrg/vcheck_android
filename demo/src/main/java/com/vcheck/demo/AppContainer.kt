package com.vcheck.demo

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer {

    private var verificationRetrofit: Retrofit

    private var partnerRetrofit: Retrofit

    init {
        verificationRetrofit = getVerifApiRetrofit()
        partnerRetrofit = getPartnerApiRetrofit()
    }

    private fun getHttpClient(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor()

        val httpClient = OkHttpClient.Builder()

        // For tests:
        httpClient.addInterceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder().build()
            val hasMultipart: Boolean = request.headers.names().contains("multipart")
            logging.setLevel(if (hasMultipart) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY)
            chain.proceed(request)
        }.build()

        logging.setLevel(HttpLoggingInterceptor.Level.NONE)

        httpClient.addInterceptor(logging)
        httpClient.readTimeout(180, TimeUnit.SECONDS) //3min
        httpClient.connectTimeout(180, TimeUnit.SECONDS) //3min

        return httpClient
    }

    private fun getVerifApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(getHttpClient().build())
            .baseUrl(ConstantsProvider.VERIFICATIONS_API_BASE_URL) //TEST(DEV)
            .build()
    }

    private fun getPartnerApiRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(getHttpClient().build())
            .baseUrl(ConstantsProvider.PARTNER_API_BASE_URL) //TEST(DEV)
            .build()
    }

    var datasource = Datasource(
        verificationRetrofit.create(VerifApiClient::class.java),
        partnerRetrofit.create(PartnerApiClient::class.java))
}
