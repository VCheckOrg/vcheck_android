package com.vcheck.demo

import android.util.Log
import com.vcheck.demo.app.*
import com.vcheck.demo.ConstantsProvider.Companion.PARTNER_ID
import com.vcheck.demo.ConstantsProvider.Companion.PARTNER_SECRET
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.domain.toStringRepresentation
import retrofit2.Call
import java.util.*

class Datasource(private val verificationApiClient: VerifApiClient,
                 private val partnerApiClient: PartnerApiClient
) {

    private var verificationId: Int? = null

    fun setVerificationId(id: Int) {
        this.verificationId = id
    }

    fun getVerificationId(): Int {
        if (verificationId == null) {
            throw RuntimeException("VCheck Demo - error: verification id not set!")
        }
        return verificationId ?: -1
    }

    fun getServiceTimestamp() : Call<String> {
        return verificationApiClient.getServiceTimestamp()
    }

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            Call<CreateVerificationAttemptResponse> {
        return partnerApiClient.createVerificationRequest(verificationRequestBody)
    }

    fun checkFinalVerificationStatus()
            : Call<FinalVerifCheckResponseModel>? {
        val timestampResponse = getServiceTimestamp().execute()
        return if (timestampResponse.isSuccessful) {
            val timestamp = (timestampResponse.body() as String).toInt()
            val strToSign = "$PARTNER_ID$timestamp${getVerificationId()}$PARTNER_SECRET"
            val sign = generateSHA256Hash(strToSign)
            partnerApiClient.checkFinalVerificationStatus(
                VCheckSDK.getVerificationToken(), getVerificationId(), PARTNER_ID, timestamp, sign)
        } else {
            Log.d("VCheck - error: ","Cannot get service timestamp for check verification call!")
            null
        }
    }

    fun sendPartnerApplicationRequest(body: PartnerApplicationRequestData)
    : Call<FinalVerifCheckResponseModel> {
        return partnerApiClient.sendPartnerApplicationRequest(body)
    }

    fun prepareVerificationRequest(serviceTS: Long, deviceDefaultLocaleCode: String,
                                   vModel: VerificationClientCreationModel
    ): CreateVerificationRequestBody {

        val partnerId = vModel.partnerId
        val partnerSecret = vModel.partnerSecret
        val scheme = vModel.verificationType.toStringRepresentation()
        val partnerUserId = vModel.partnerUserId ?: Date().time.toString()
        val partnerVerificationId = vModel.partnerVerificationId ?: Date().time.toString()
        val verifCallbackURL = "${ConstantsProvider.VERIFICATIONS_API_BASE_URL}ping"
        val sign = generateSHA256Hash(
            "$partnerId$partnerUserId$partnerVerificationId$scheme$serviceTS$partnerSecret")

        return CreateVerificationRequestBody(
            partner_id = partnerId,
            timestamp = serviceTS,
            scheme = scheme,
            locale = deviceDefaultLocaleCode,
            partner_user_id = partnerUserId,
            partner_verification_id = partnerVerificationId,
            callback_url = verifCallbackURL,
            sign = sign)
    }
}