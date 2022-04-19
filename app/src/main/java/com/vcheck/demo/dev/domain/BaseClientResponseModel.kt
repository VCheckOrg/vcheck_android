package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class BaseClientResponseModel(
    @SerializedName("data")
    val data: Any?,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = "")


