package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody

class MainRepository(private val remoteDatasource : RemoteDatasource) {

//    suspend fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody): CreateVerificationAttemptResponse
//        = remoteData.createVerificationRequest(verificationRequestBody)

    fun createTestVerificationRequest() : MutableLiveData<Resource<CreateVerificationAttemptResponse>>
            = remoteDatasource.createVerificationRequest(CreateVerificationRequestBody())

    fun initVerification(verifToken: String) : MutableLiveData<Resource<VerificationInitResponse>> {
        //val token = localDatasource.getVerifToken(context)
        return if (verifToken.isNotEmpty()) {
            remoteDatasource.initVerification(verifToken)
        } else MutableLiveData(Resource.error(ApiError("No token available!")))
    }

    fun getCountries(verifToken: String) : MutableLiveData<Resource<CountriesResponse>> {
        return if (verifToken.isNotEmpty()) {
            remoteDatasource.getCountries(verifToken)
        } else MutableLiveData(Resource.error(ApiError("No token available!")))
    }

    fun getCountryAvailableDocTypeInfo(verifToken: String, countryId: Int)
        : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return remoteDatasource.getCountryAvailableDocTypeInfo(verifToken, countryId)
    }

    fun uploadVerificationDocument(
        token: String,
        image: MultipartBody.Part
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return if (token.isNotEmpty()) {
            remoteDatasource.uploadVerificationDocument(token, DocumentUploadRequestBody(), image)
        } else MutableLiveData(Resource.error(ApiError("No token available!")))
    }
}