package com.vcheck.demo.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.domain.CreateVerificationRequestBody

class RemoteDatasource (private val apiClient: ApiClient) {

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            MutableLiveData<Resource<CreateVerificationAttemptResponse>> {
        return NetworkCall<CreateVerificationAttemptResponse>().makeCall(apiClient.createVerificationRequest(
            verificationRequestBody
        ))
    }
}