package com.vcheck.demo

import retrofit2.Call
import retrofit2.http.GET

interface VerifApiClient {

    @GET("timestamp")
    fun getServiceTimestamp() : Call<String>
}