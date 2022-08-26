package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

data class BaseClientResponseModel(
    @SerializedName("data")
    val data: BaseClientResponseData?,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = "")

data class BaseClientResponseData(
    //optional id of document etc. which may come with 400 code
    @SerializedName("id")
    val id: Int?
)
