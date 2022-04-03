package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.CreateVerificationRequestBody

class MainRepository(private val remoteData: RemoteDatasource) {

//    suspend fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody): CreateVerificationAttemptResponse
//        = remoteData.createVerificationRequest(verificationRequestBody)

    fun createTestVerificationRequest(): MutableLiveData<Resource<CreateVerificationAttemptResponse>> =
        remoteData.createVerificationRequest(CreateVerificationRequestBody())
}