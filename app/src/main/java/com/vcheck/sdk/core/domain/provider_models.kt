package com.vcheck.sdk.core.domain

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ProvidersResponse (
    @SerializedName("data")
    val data: List<Provider>,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = "")

@Parcelize
data class Provider(
    @SerializedName("id")
    val id: Int, //ID
    @SerializedName("name")
    val name: String, //имя провайдера
    @SerializedName("protocol")
    val protocol: String, //протокол работы: Vcheck,  Дия и так далее. Сейчас может быть только vcheck
    @SerializedName("countries")
    val countries: List<String>? //список стран для которых доступен провайдер. Может быть null, это значит что для данного провайдера страна не важна.
): Parcelable

data class InitProviderRequestBody(
    @SerializedName("provider_id")
    val providerId: Int,
    @SerializedName("country")
    val country: String?
)

