package com.vcheck.demo.dev.domain

import com.vcheck.demo.dev.util.generateSHA256Hash
import java.util.*

data class CreateVerificationRequestBody(
    val partner_id: Int = 1,
    val partner_application_id: String = System.currentTimeMillis().toString(),
    val partner_user_id: String = System.currentTimeMillis().toString(),
//    val partner_application_url: String? = null,
//    val partner_user_url: String? = null,
//    val return_url: String? = null,
//    val callback_url: String? = null,
//    val session_lifetime: Int? = null,
    val timestamp: Long = Date().time / 1000,
    val locale: String = "ru",
    val sign: String = generateSHA256Hash(
        partner_application_id + "$partner_id" +
            partner_user_id + "$timestamp" + "DWBnN7LbeTaqG9vE"))
            //client secret key at the end; currently hardcoded for tests!

