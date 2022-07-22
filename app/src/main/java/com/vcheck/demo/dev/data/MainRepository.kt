package com.vcheck.demo.dev.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.util.generateSHA256Hash
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import java.util.*

class MainRepository(
    private val remoteDatasource: RemoteDatasource,
    private val localDatasource: LocalDatasource) {

    private fun isTokenPresent(): Boolean {
        return VCheckSDK.getVerificationToken().isNotEmpty()
    }

    fun prepareVerificationRequest(serviceTS: Long, deviceDefaultLocaleCode: String,
            vModel: VerificationClientCreationModel): CreateVerificationRequestBody {

        val partnerId = vModel.partnerId
        val partnerSecret = vModel.partnerSecret
        val scheme = vModel.verificationType.toStringRepresentation()
        val partnerUserId = vModel.partnerUserId ?: Date().time.toString()
        val partnerVerificationId = vModel.partnerVerificationId ?: Date().time.toString()
        val sessionLifetime = vModel.sessionLifetime ?: RemoteApiConfigProvider.DEFAULT_SESSION_LIFETIME
        val verifCallbackURL = "${RemoteApiConfigProvider.VERIFICATIONS_API_BASE_URL}ping"

        return CreateVerificationRequestBody(
                partner_id = partnerId,
                timestamp = serviceTS,
                scheme = scheme,
                locale = deviceDefaultLocaleCode,
                partner_user_id = partnerUserId,
                partner_verification_id = partnerVerificationId,
                callback_url = verifCallbackURL,
                session_lifetime = sessionLifetime,
                sign = generateSHA256Hash(
                    "$partnerId$partnerUserId$partnerVerificationId$scheme$serviceTS$partnerSecret"))
    }

    fun createVerification(createVerificationRequestBody: CreateVerificationRequestBody)
        : MutableLiveData<Resource<CreateVerificationAttemptResponse>> {
        return remoteDatasource.createVerificationRequest(createVerificationRequestBody)
    }

    fun initVerification(): MutableLiveData<Resource<VerificationInitResponse>> {
        return if (isTokenPresent()) remoteDatasource.initVerification()
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getCountries(): MutableLiveData<Resource<CountriesResponse>> {
        return if (isTokenPresent()) remoteDatasource.getCountries()
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getCountryAvailableDocTypeInfo(countryCode: String)
            : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return if (isTokenPresent()) remoteDatasource.getCountryAvailableDocTypeInfo(countryCode)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun uploadVerificationDocuments(
        documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return if (isTokenPresent()) remoteDatasource.uploadVerificationDocuments(documentUploadRequestBody, images)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getDocumentInfo(docId: Int): MutableLiveData<Resource<PreProcessedDocumentResponse>> {
        return if (isTokenPresent()) remoteDatasource.getDocumentInfo(docId)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun updateAndConfirmDocInfo(
        docId: Int,
        docData: DocUserDataRequestBody,
    ): MutableLiveData<Resource<Response<Void>>> {
        return if (isTokenPresent()) remoteDatasource.updateAndConfirmDocInfo(docId, docData)
        else MutableLiveData(Resource.error(ApiError(null, BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun uploadLivenessVideo(video: MultipartBody.Part)
        : MutableLiveData<Resource<LivenessUploadResponse>> {
        return if (isTokenPresent()) remoteDatasource.uploadLivenessVideo(video)
        else MutableLiveData(Resource.error(ApiError(null,BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getActualServiceTimestamp() : MutableLiveData<Resource<String>> {
        return remoteDatasource.getServiceTimestamp()
    }

    fun getCurrentStage(): MutableLiveData<Resource<StageResponse>> {
        return if (isTokenPresent()) remoteDatasource.getCurrentStage()
        else MutableLiveData(Resource.error(ApiError(null,BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun sendLivenessGestureAttempt(
        image: MultipartBody.Part,
        gesture: MultipartBody.Part): MutableLiveData<Resource<LivenessGestureResponse>> {
        return if (isTokenPresent()) remoteDatasource.sendLivenessGestureAttempt(image, gesture)
        else MutableLiveData(Resource.error(ApiError(null,BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun checkFinalVerificationStatus(verifId: Int): Call<FinalVerifCheckResponseModel> {
        return if (isTokenPresent()) remoteDatasource.checkFinalVerificationStatus(verifId)
        else throw RuntimeException("VCheckSDK - error: token is not present while checking verification status!")
    }

    //---- LOCAL SOURCE DATA OPS:

    fun storeSelectedCountryCode(ctx: Context, countryCode: String) {
        localDatasource.storeSelectedCountryCode(ctx, countryCode)
    }

    fun getSelectedCountryCode(ctx: Context): String {
        return localDatasource.getSelectedCountryCode(ctx)
    }

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

    fun resetCacheOnStartup(ctx: Context) {
        localDatasource.resetCacheOnStartup(ctx)
    }
}

//    fun setDocumentAsPrimary(token: String, docId: Int) : MutableLiveData<Resource<Response<Void>>> {
//        return if (token.isNotEmpty()) {
//            remoteDatasource.setDocumentAsPrimary(token, docId)
//        } else {
//            MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
//        }
//    }
//    fun checkIfApiConfigShouldBeChanged(vModel: VerificationClientCreationModel): Boolean {
//        var shouldUpdateApi = false
//
//        if (vModel.customVerificationServiceURL != null) {
//            remoteApiConfigProvider.setVerificationsApiBaseUrl(vModel.customVerificationServiceURL!!)
//            shouldUpdateApi = true
//        }
//        if (vModel.customPartnerServiceURL != null) {
//            remoteApiConfigProvider.setPartnerApiBaseUrl(vModel.customPartnerServiceURL!!)
//            shouldUpdateApi = true
//        }
//        return shouldUpdateApi
//    }

// Could do with app context + SharedPrefs!
//fun storeVerifToken(ctx: Context, verifToken: String) {
//    localDatasource.storeVerifToken(ctx, verifToken)
//}
//
//fun getVerifToken(ctx: Context): String {
//    return localDatasource.getVerifToken(ctx)
//}