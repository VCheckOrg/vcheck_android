package com.vcheck.demo.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.domain.CreateVerificationRequestBody

class MainRepository(private val remoteData : RemoteDatasource) {

//    suspend fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody): CreateVerificationAttemptResponse
//        = remoteData.createVerificationRequest(verificationRequestBody)

    fun createTestVerificationRequest() : MutableLiveData<Resource<CreateVerificationAttemptResponse>>
            = remoteData.createVerificationRequest(CreateVerificationRequestBody())
}