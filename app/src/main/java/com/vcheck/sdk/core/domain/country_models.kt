package com.vcheck.sdk.core.domain

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class CountriesResponse (
    @SerializedName("data")
    val data: List<Country>,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = "")

data class Country(
    @SerializedName("code")
    val code: String,
    @SerializedName("is_blocked")
    val isBlocked: Boolean)

@Parcelize
data class CountryTO(val name: String, val code: String, val flag: String, val isBlocked : Boolean): Parcelable