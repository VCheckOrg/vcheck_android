package com.vcheck.demo.dev.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Response

class RemoteDatasource(private val verificationApiClient: VerificationApiClient,
                       private val partnerApiClient: PartnerApiClient) {

    /*
    https://test-verification-new.vycheck.com/api/v1/ - verification_api (TEST)
    https://test-partner.vycheck.com/api/v1/ - partner_api
    первая правда может поменятся, планируем убрать new как снесем старый тест
     */

    fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody):
            MutableLiveData<Resource<CreateVerificationAttemptResponse>> {
        return NetworkCall<CreateVerificationAttemptResponse>().makeCall(
            partnerApiClient.createVerificationRequest(verificationRequestBody)
        )
    }

    fun initVerification(verifToken: String): MutableLiveData<Resource<VerificationInitResponse>> {
        return NetworkCall<VerificationInitResponse>().makeCall(
            verificationApiClient.initVerification(verifToken)
        )
    }

    fun getCountries(verifToken: String): MutableLiveData<Resource<CountriesResponse>> {
        return NetworkCall<CountriesResponse>().makeCall(
            verificationApiClient.getCountries(verifToken)
        )
    }

    fun getCountryAvailableDocTypeInfo(verifToken: String, countryCode: String)
            : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return NetworkCall<DocumentTypesForCountryResponse>().makeCall(
            verificationApiClient.getCountryAvailableDocTypeInfo(verifToken, countryCode))
    }

    fun uploadVerificationDocuments(
        verifToken: String,
        documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        if (images.size == 1) {
            return NetworkCall<DocumentUploadResponse>().makeCall(
                verificationApiClient.uploadVerificationDocumentsForOnePage(
                    verifToken,
                    images[0],
                    MultipartBody.Part.createFormData("country", documentUploadRequestBody.country),
                    MultipartBody.Part.createFormData("category", documentUploadRequestBody.document_type.toString()),
                ))
        }
        else {
            return NetworkCall<DocumentUploadResponse>().makeCall(verificationApiClient.uploadVerificationDocumentsForTwoPages(
                verifToken,
                images[0],
                images[1],
                MultipartBody.Part.createFormData("country", documentUploadRequestBody.country),
                MultipartBody.Part.createFormData("category", documentUploadRequestBody.document_type.toString())
            ))
        }
    }

    fun getDocumentInfo(verifToken: String, docId: Int)
            : MutableLiveData<Resource<PreProcessedDocumentResponse>> {
        return NetworkCall<PreProcessedDocumentResponse>().makeCall(
            verificationApiClient.getDocumentInfo(verifToken, docId))
    }

    fun updateAndConfirmDocInfo(
        verifToken: String,
        docId: Int,
        docData: DocUserDataRequestBody
    ): MutableLiveData<Resource<Response<Void>>> {
        return NetworkCall<Response<Void>>().makeCall(
            verificationApiClient.updateAndConfirmDocInfo(verifToken, docId, docData))
    }

    fun getServiceTimestamp() : MutableLiveData<Resource<String>> {
        return NetworkCall<String>().makeCall(
            verificationApiClient.getServiceTimestamp())
    }

    fun uploadLivenessVideo(verifToken: String, video: MultipartBody.Part)
        : MutableLiveData<Resource<LivenessUploadResponse>> {
        return NetworkCall<LivenessUploadResponse>().makeCall(verificationApiClient.uploadLivenessVideo(
            verifToken, video))
    }

    fun getCurrentStage(
        verifToken: String
    ) : MutableLiveData<Resource<StageResponse>> {
        return NetworkCall<StageResponse>().makeCall(verificationApiClient.getCurrentStage(verifToken))
    }
}


//Test:
//        val type = (0..1).random()
//        return if ((0..1).random() == 1) {
//            MutableLiveData(Resource.success(StageResponse(data = StageResponseData(
//                id = 0, type = type), errorCode = 1, message = "USER_INTERACTED_COMPLETED")))
//        } else {
//            MutableLiveData(Resource.success(StageResponse(data = StageResponseData(
//                id = 0, type = type), errorCode = 0, message = "VERIFICATION_NOT_INITIALIZED")))
//        }

//Deprecated:
//    fun setDocumentAsPrimary(verifToken: String, docId: Int) : MutableLiveData<Resource<Response<Void>>> {
//        return NetworkCall<Response<Void>>().makeCall(verificationApiClient.setDocumentAsPrimary(
//            verifToken, docId))
//    }