package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response

class RemoteDatasource(private val verificationApiClient: VerificationApiClient,
                       private val partnerApiClient: PartnerApiClient) {

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            MutableLiveData<Resource<CreateVerificationAttemptResponse>> {
        return NetworkCall<CreateVerificationAttemptResponse>().makeCall(
            partnerApiClient.createVerificationRequest(verificationRequestBody)
        )
    }

    fun initVerification(): MutableLiveData<Resource<VerificationInitResponse>> {
        return NetworkCall<VerificationInitResponse>().makeCall(
            verificationApiClient.initVerification(VCheckSDK.getVerificationToken())
        )
    }

    fun getCountries(): MutableLiveData<Resource<CountriesResponse>> {
        return NetworkCall<CountriesResponse>().makeCall(
            verificationApiClient.getCountries(VCheckSDK.getVerificationToken())
        )
    }

    fun getCountryAvailableDocTypeInfo(countryCode: String)
            : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return NetworkCall<DocumentTypesForCountryResponse>().makeCall(
            verificationApiClient.getCountryAvailableDocTypeInfo(VCheckSDK.getVerificationToken(), countryCode))
    }

    fun uploadVerificationDocuments(
        documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        if (images.size == 1) {
            return NetworkCall<DocumentUploadResponse>().makeCall(
                verificationApiClient.uploadVerificationDocumentsForOnePage(
                    VCheckSDK.getVerificationToken(),
                    images[0],
                    MultipartBody.Part.createFormData("country", documentUploadRequestBody.country),
                    MultipartBody.Part.createFormData("category", documentUploadRequestBody.document_type.toString()),
                ))
        }
        else {
            return NetworkCall<DocumentUploadResponse>().makeCall(verificationApiClient.uploadVerificationDocumentsForTwoPages(
                VCheckSDK.getVerificationToken(),
                images[0],
                images[1],
                MultipartBody.Part.createFormData("country", documentUploadRequestBody.country),
                MultipartBody.Part.createFormData("category", documentUploadRequestBody.document_type.toString())
            ))
        }
    }

    fun getDocumentInfo(docId: Int)
            : MutableLiveData<Resource<PreProcessedDocumentResponse>> {
        return NetworkCall<PreProcessedDocumentResponse>().makeCall(
            verificationApiClient.getDocumentInfo(VCheckSDK.getVerificationToken(), docId))
    }

    fun updateAndConfirmDocInfo(
        docId: Int,
        docData: DocUserDataRequestBody
    ): MutableLiveData<Resource<Response<Void>>> {
        return NetworkCall<Response<Void>>().makeCall(
            verificationApiClient.updateAndConfirmDocInfo(VCheckSDK.getVerificationToken(), docId, docData))
    }

    fun getServiceTimestamp() : MutableLiveData<Resource<String>> {
        return NetworkCall<String>().makeCall(
            verificationApiClient.getServiceTimestamp())
    }

    fun uploadLivenessVideo(video: MultipartBody.Part)
        : MutableLiveData<Resource<LivenessUploadResponse>> {
        return NetworkCall<LivenessUploadResponse>().makeCall(verificationApiClient.uploadLivenessVideo(
            VCheckSDK.getVerificationToken(), video))
    }

    fun getCurrentStage(
    ) : MutableLiveData<Resource<StageResponse>> {
        return NetworkCall<StageResponse>().makeCall(verificationApiClient.getCurrentStage(VCheckSDK.getVerificationToken()))
    }

    fun sendLivenessGestureAttempt(
        image: MultipartBody.Part,
        gesture: MultipartBody.Part): MutableLiveData<Resource<LivenessGestureResponse>> {
        return NetworkCall<LivenessGestureResponse>().makeCall(
            verificationApiClient.sendLivenessGestureAttempt(VCheckSDK.getVerificationToken(), image, gesture))
    }

    fun checkFinalVerificationStatus(verifId: Int)
    : Call<FinalVerifCheckResponseModel> {
        return partnerApiClient.checkFinalVerificationStatus(
                VCheckSDK.getVerificationToken(), verifId)
    }
}