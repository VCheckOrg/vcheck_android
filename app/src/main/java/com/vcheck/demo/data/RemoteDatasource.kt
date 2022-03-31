package com.vcheck.demo.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.domain.CreateVerificationRequestBody
import retrofit2.Response

class RemoteDatasource (private val apiClient: ApiClient) {

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            MutableLiveData<Resource<CreateVerificationAttemptResponse>> {
        return NetworkCall<CreateVerificationAttemptResponse>().makeCall(apiClient.createVerificationRequest(
            verificationRequestBody
        ))
    }
}