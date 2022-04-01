package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.CreateVerificationRequestBody

class RemoteDatasource (private val apiClient: ApiClient) {

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            MutableLiveData<Resource<CreateVerificationAttemptResponse>> {
        return NetworkCall<CreateVerificationAttemptResponse>().makeCall(apiClient.createVerificationRequest(
            verificationRequestBody
        ))
    }
}