package com.vcheck.demo.dev.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.util.generateSHA256Hash
import okhttp3.MultipartBody
import retrofit2.Response
import java.util.*

class MainRepository(
    private val remoteDatasource: RemoteDatasource,
    private val localDatasource: LocalDatasource
) {

    fun createVerificationRequest(serviceTS: Long, deviceDefaultLocaleCode: String,
            vModel: VerificationClientCreationModel)
        : MutableLiveData<Resource<CreateVerificationAttemptResponse>> {

        val partnerId = vModel.partnerId
        val partnerSecret = vModel.partnerSecret
        val scheme = vModel.verificationType.toStringRepresentation()
        val partnerUserId = vModel.partnerUserId ?: Date().time.toString()
        val partnerVerificationId = vModel.partnerVerificationId ?: Date().time.toString()
        val callbackUrl = if (vModel.customServiceURL != null)
            "${vModel.customServiceURL}/ping" else "${RemoteDatasource.VERIFICATIONS_API_BASE_URL}/ping"
        val sessionLifetime = vModel.sessionLifetime ?: RemoteDatasource.DEFAULT_SESSION_LIFETIME

        return remoteDatasource.createVerificationRequest(
            CreateVerificationRequestBody(
                partner_id = partnerId,
                timestamp = serviceTS,
                scheme = scheme,
                locale = deviceDefaultLocaleCode,
                partner_user_id = partnerUserId,
                partner_verification_id = partnerVerificationId,
                callback_url = callbackUrl,
                session_lifetime = sessionLifetime,
                sign = generateSHA256Hash(
                    "$partnerId$partnerUserId$partnerVerificationId$scheme$serviceTS$partnerSecret")))
    }

    fun initVerification(verifToken: String): MutableLiveData<Resource<VerificationInitResponse>> {
        return if (verifToken.isNotEmpty()) {
            remoteDatasource.initVerification(verifToken)
        } else MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getCountries(verifToken: String): MutableLiveData<Resource<CountriesResponse>> {
        return if (verifToken.isNotEmpty()) {
            remoteDatasource.getCountries(verifToken)
        } else MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getCountryAvailableDocTypeInfo(verifToken: String, countryCode: String)
            : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return if (verifToken.isNotEmpty()) {
            return remoteDatasource.getCountryAvailableDocTypeInfo(verifToken, countryCode)
        } else MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun uploadVerificationDocuments(
        verifToken: String,
        documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return if (verifToken.isNotEmpty()) {
            remoteDatasource.uploadVerificationDocuments(
                verifToken,
                documentUploadRequestBody,
                images
            )
        } else MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getDocumentInfo(
        token: String,
        docId: Int
    ): MutableLiveData<Resource<PreProcessedDocumentResponse>> {
        return if (token.isNotEmpty()) {
            remoteDatasource.getDocumentInfo(token, docId)
        } else {
            MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
        }
    }

    fun updateAndConfirmDocInfo(
        token: String,
        docId: Int,
        docData: ParsedDocFieldsData
    ): MutableLiveData<Resource<Response<Void>>> {
        return if (token.isNotEmpty()) {
            remoteDatasource.updateAndConfirmDocInfo(token, docId, docData)
        } else {
            MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
        }
    }

    fun setDocumentAsPrimary(token: String, docId: Int) : MutableLiveData<Resource<Response<Void>>> {
        return if (token.isNotEmpty()) {
            remoteDatasource.setDocumentAsPrimary(token, docId)
        } else {
            MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
        }
    }

    fun uploadLivenessVideo(verifToken: String, video: MultipartBody.Part)
        : MutableLiveData<Resource<LivenessUploadResponse>> {
        return if (verifToken.isNotEmpty()) {
            remoteDatasource.uploadLivenessVideo(verifToken, video)
        } else MutableLiveData(Resource.error(ApiError(BaseClientErrors.NO_TOKEN_AVAILABLE)))
    }

    fun getActualServiceTimestamp() : MutableLiveData<Resource<String>> {
        return remoteDatasource.getServiceTimestamp()
    }

    fun getCurrentStage(): MutableLiveData<Resource<StageResponse>> {
        return remoteDatasource.getCurrentStage()
    }

    //---- LOCAL SOURCE DATA OPS:

    fun storeVerifToken(ctx: Context, verifToken: String) {
        localDatasource.storeVerifToken(ctx, verifToken)
    }

    fun getVerifToken(ctx: Context): String {
        return localDatasource.getVerifToken(ctx)
    }

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

    fun resetCacheOnStartup(ctx: Context) {
        localDatasource.resetCacheOnStartup(ctx)
    }
}