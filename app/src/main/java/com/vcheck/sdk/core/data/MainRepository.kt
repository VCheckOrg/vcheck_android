package com.vcheck.sdk.core.data

import androidx.lifecycle.MutableLiveData
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.transferrable_objects.CheckPhotoDataTO
import okhttp3.MultipartBody
import retrofit2.Response

class MainRepository(
    private val remoteDatasource: RemoteDatasource,
    private val localDatasource: LocalDatasource) {

    private fun isTokenPresent(): Boolean {
        return VCheckSDK.getVerificationToken().isNotEmpty()
    }

    fun getProviders(): MutableLiveData<Resource<ProvidersResponse>> {
        return if (isTokenPresent()) remoteDatasource.getProviders()
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun getPriorityCountries(): MutableLiveData<Resource<PriorityCountries>> {
        return if (isTokenPresent()) remoteDatasource.getPriorityCountries()
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun initProvider(initProviderRequestBody: InitProviderRequestBody)
            : MutableLiveData<Resource<Response<Void>>> {
        return if (isTokenPresent()) remoteDatasource.initProvider(initProviderRequestBody)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun initVerification(): MutableLiveData<Resource<VerificationInitResponse>> {
        return if (isTokenPresent()) remoteDatasource.initVerification()
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun getCountryAvailableDocTypeInfo(countryCode: String)
            : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return if (isTokenPresent()) remoteDatasource.getCountryAvailableDocTypeInfo(countryCode)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun uploadVerificationDocuments(
        documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return if (isTokenPresent()) remoteDatasource.uploadVerificationDocuments(documentUploadRequestBody, images)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun getDocumentInfo(docId: Int): MutableLiveData<Resource<PreProcessedDocumentResponse>> {
        return if (isTokenPresent()) remoteDatasource.getDocumentInfo(docId)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun updateAndConfirmDocInfo(
        docId: Int,
        docData: DocUserDataRequestBody,
    ): MutableLiveData<Resource<Response<Void>>> {
        return if (isTokenPresent()) remoteDatasource.updateAndConfirmDocInfo(docId, docData)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun uploadLivenessVideo(video: MultipartBody.Part)
        : MutableLiveData<Resource<LivenessUploadResponse>> {
        return if (isTokenPresent()) remoteDatasource.uploadLivenessVideo(video)
        else MutableLiveData(Resource.error(ApiError(null,BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun getActualServiceTimestamp() : MutableLiveData<Resource<String>> {
        return remoteDatasource.getServiceTimestamp()
    }

    fun getCurrentStage(): MutableLiveData<Resource<StageResponse>> {
        return if (isTokenPresent()) remoteDatasource.getCurrentStage()
        else MutableLiveData(Resource.error(ApiError(null,BaseClientErrors.NO_TOKEN_AVAILABLE_STR)))
    }

    fun sendLivenessGestureAttempt(
        image: MultipartBody.Part,
        gesture: MultipartBody.Part): LivenessGestureResponse? {
        return if (isTokenPresent()) remoteDatasource.sendLivenessGestureAttempt(image, gesture)
        else null
    }

    fun sendSegmentationDocAttempt(
        image: MultipartBody.Part,
        country: String,
        category: String,
        index: String): SegmentationGestureResponse? {
        return if (isTokenPresent()) remoteDatasource.sendSegmentationDocAttempt(image, country, category, index)
        else null
    }

    //---- LOCAL SOURCE DATA OPS:

    fun setSelectedDocTypeWithData(data: DocTypeData) {
        localDatasource.setSelectedDocTypeWithData(data)
    }

    fun getSelectedDocTypeWithData(): DocTypeData? {
        return localDatasource.getSelectedDocTypeWithData()
    }

    fun setLivenessMilestonesList(list: List<String>) {
        localDatasource.setLivenessMilestonesList(list)
    }

    fun getLivenessMilestonesList(): List<String>? {
        return localDatasource.getLivenessMilestonesList()
    }

    fun resetCache() {
        localDatasource.resetCache()
    }

    fun setCheckDocPhotosTO(data: CheckPhotoDataTO) {
        localDatasource.setCheckDocPhotosTO(data)
    }

    fun getCheckDocPhotosTO(): CheckPhotoDataTO? {
        return localDatasource.getCheckDocPhotosTO()
    }

    fun setFinishStartupActivity(s: Boolean) {
        localDatasource.setFinishStartupActivity(s)
    }

    fun shouldFinishStartupActivity(): Boolean {
        return localDatasource.shouldFinishStartupActivity()
    }

    fun setFirePartnerCallback(s: Boolean) {
        localDatasource.setFirePartnerCallback(s)
    }

    fun shouldFirePartnerCallback(): Boolean {
        return localDatasource.shouldFirePartnerCallback()
    }

    fun setManualPhotoUpload() {
        localDatasource.setManualPhotoUpload()
    }

    fun isPhotoUploadManual(): Boolean {
        return localDatasource.isPhotoUploadManual()
    }
}