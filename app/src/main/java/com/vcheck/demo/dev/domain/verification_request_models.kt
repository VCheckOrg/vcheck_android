package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName
import com.vcheck.demo.dev.util.generateSHA256Hash
import java.util.*

data class CreateVerificationRequestBody(
    @SerializedName("partner_id")
    val partner_id: Int = 1,
    @SerializedName("partner_application_id")
    val partner_application_id: String = Date().time.toString(),
    @SerializedName("partner_user_id")
    val partner_user_id: String = Date().time.toString(),
    @SerializedName("timestamp")
    val timestamp: Long,
    //= (Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000) - 3,
    @SerializedName("locale")
    val locale: String,
    @SerializedName("sign")
    val sign: String = generateSHA256Hash(
        "$partner_application_id$partner_id$partner_user_id$timestamp" + "DWBnN7LbeTaqG9vE")) //DWBnN7LbeTaqG9vE
            //client secret key at the end; currently hardcoded for tests!


//    Obsolete fields:
//    val partner_application_url: String? = null,
//    val partner_user_url: String? = null,
//    val return_url: String? = null,
//    val callback_url: String? = null,
//    val session_lifetime: Int? = null,

/*
    data = {
            partner_id: PARTNER_ID,
            partner_application_id: Date.now().toString(),
            partner_user_id: Date.now().toString(),
            timestamp: Math.floor(Date.now() / 1000),
          };
 */