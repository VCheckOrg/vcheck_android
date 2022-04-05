package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody

class RemoteDatasource(private val apiClient: ApiClient) {

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            MutableLiveData<Resource<CreateVerificationAttemptResponse>> {

        return NetworkCall<CreateVerificationAttemptResponse>().makeCall(
            apiClient.createVerificationRequest(
                verificationRequestBody
            )
        )
    }

    fun initVerification(verifToken: String): MutableLiveData<Resource<VerificationInitResponse>> {
        return NetworkCall<VerificationInitResponse>().makeCall(apiClient.initVerification(
            verifToken
        ))
    }

    fun getCountries(verifToken: String): MutableLiveData<Resource<CountriesResponse>> {
        return NetworkCall<CountriesResponse>().makeCall(apiClient.getCountries(
            verifToken
        ))
    }

    fun getCountryAvailableDocTypeInfo(verifToken: String, countryId: Int)
        : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return NetworkCall<DocumentTypesForCountryResponse>().makeCall(
            apiClient.getCountryAvailableDocTypeInfo(verifToken, countryId)
        )
    }

    fun uploadVerificationDocument(
        verifToken: String,
        documentUploadRequestBody: DocumentUploadRequestBody,
        image: MultipartBody.Part
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return NetworkCall<DocumentUploadResponse>().makeCall(
            apiClient.uploadVerificationDocument(
                verifToken,
                documentUploadRequestBody,
                image
            )
        )
    }


}