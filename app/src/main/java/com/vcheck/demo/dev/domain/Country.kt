package com.vcheck.demo.dev.domain

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class CountriesResponse (
    @SerializedName("data")
    val data: List<Country>,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

@Parcelize
data class Country(val name: String, val flag: Int): Parcelable