package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Response

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
        return NetworkCall<VerificationInitResponse>().makeCall(
            apiClient.initVerification(
                verifToken
            )
        )
    }

    fun getCountries(verifToken: String): MutableLiveData<Resource<CountriesResponse>> {
        return NetworkCall<CountriesResponse>().makeCall(
            apiClient.getCountries(
                verifToken
            )
        )
    }

    fun getCountryAvailableDocTypeInfo(verifToken: String, countryCode: String)
            : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return NetworkCall<DocumentTypesForCountryResponse>().makeCall(
            apiClient.getCountryAvailableDocTypeInfo(verifToken, countryCode)
        )
    }

    fun uploadVerificationDocuments(
        verifToken: String,
        documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return NetworkCall<DocumentUploadResponse>().makeCall(
            apiClient.uploadVerificationDocuments(
                verifToken,
                images[0],
                images.getOrNull(1),
                MultipartBody.Part.createFormData("country", documentUploadRequestBody.country),
                MultipartBody.Part.createFormData(
                    "document_type",
                    documentUploadRequestBody.document_type.toString()
                ),
                MultipartBody.Part.createFormData(
                    "is_handwritten",
                    documentUploadRequestBody.is_handwritten.toString()
                )
            )
        )
    }

    fun getDocumentInfo(verifToken: String, docId: Int)
            : MutableLiveData<Resource<PreProcessedDocumentResponse>> {
        return NetworkCall<PreProcessedDocumentResponse>().makeCall(
            apiClient.getDocumentInfo(verifToken, docId)
        )
    }

    fun updateAndConfirmDocInfo(
        verifToken: String,
        docId: Int,
        docData: ParsedDocFieldsData
    ): MutableLiveData<Resource<Response<Void>>> {
        return NetworkCall<Response<Void>>().makeCall(
            apiClient.updateAndConfirmDocInfo(verifToken, docId, docData)
        )
    }
}