package com.vcheck.demo.dev.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.ApiError
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.CreateVerificationRequestBody
import com.vcheck.demo.dev.domain.VerificationInitResponse

class MainRepository(private val remoteDatasource : RemoteDatasource,
    private val localDatasource: LocalDatasource) {

//    suspend fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody): CreateVerificationAttemptResponse
//        = remoteData.createVerificationRequest(verificationRequestBody)

    fun createTestVerificationRequest() : MutableLiveData<Resource<CreateVerificationAttemptResponse>>
            = remoteDatasource.createVerificationRequest(CreateVerificationRequestBody())

    fun initVerification(token: String) : MutableLiveData<Resource<VerificationInitResponse>> {
        //val token = localDatasource.getVerifToken(context)
        return if (token.isNotEmpty()) {
            remoteDatasource.initVerification(token)
        } else MutableLiveData(Resource.error(ApiError("No token available!")))
    }
}