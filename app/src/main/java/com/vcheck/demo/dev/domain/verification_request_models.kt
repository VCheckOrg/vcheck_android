package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class CreateVerificationRequestBody(
    @SerializedName("partner_id")
    val partner_id: Int,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("scheme")
    val scheme: String,
    @SerializedName("locale")
    val locale: String,
    @SerializedName("partner_user_id")
    val partner_user_id: String,
    @SerializedName("partner_verification_id")
    val partner_verification_id: String,
    @SerializedName("callback_url")
    val callback_url: String,
    @SerializedName("session_lifetime")
    val session_lifetime: Int,
    @SerializedName("sign")
    val sign: String
)


//TODO remove:

//data class CreateVerificationRequestBody(
//    @SerializedName("partner_id")
//    val partner_id: Int = 1,
//    @SerializedName("partner_application_id")
//    val partner_application_id: String = Date().time.toString(),
//    @SerializedName("partner_user_id")
//    val partner_user_id: String = Date().time.toString(),
//    @SerializedName("timestamp")
//    val timestamp: Long,
//    //= (Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000) - 3,
//    @SerializedName("locale")
//    val locale: String,
//    @SerializedName("sign")
//    val sign: String = generateSHA256Hash(
//        "$partner_application_id$partner_id$partner_user_id$timestamp" + "DWBnN7LbeTaqG9vE")) //DWBnN7LbeTaqG9vE
//            //client secret key at the end; currently hardcoded for tests!


//{
//    "partner_id": 1,
//    "timestamp": 1655983798,
//    "scheme": "full_check",
//    "locale": "en",
//    "partner_user_id": "1655983798",
//    "partner_verification_id": "1655983798",
//    "return_url": "https://example.com",
//    "callback_url": "https://example.com",
//    "session_lifetime"
//    "sign": "c528c41200827cb49480512f6483dd6bddd14c8646fd78055678b32576d6e679"
//}

//'partner_id'+'partner_user_id'+'partner_verification_id'+'scheme'+'timestamp'+'secret' !!!

//['partner_id', 'partner_user_id', 'partner_verification_id', 'scheme', 'timestamp']

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