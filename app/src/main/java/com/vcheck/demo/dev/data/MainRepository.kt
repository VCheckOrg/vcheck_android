package com.vcheck.demo.dev.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Response

class MainRepository(
    private val remoteDatasource: RemoteDatasource,
    private val localDatasource: LocalDatasource
) {

    //---- REMOTE SOURCE DATA OPS:

//  fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody): CreateVerificationAttemptResponse
//        = remoteData.createVerificationRequest(verificationRequestBody)

    fun createTestVerificationRequest(serviceTS: Long): MutableLiveData<Resource<CreateVerificationAttemptResponse>> =
        remoteDatasource.createVerificationRequest(CreateVerificationRequestBody(timestamp = serviceTS))

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

    fun getActualServiceTimestamp() : MutableLiveData<Resource<String>> {
        return remoteDatasource.getServiceTimestamp()
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

    fun getSelectedDocTypeWithData(): DocTypeData {
        return localDatasource.getSelectedDocTypeWithData()
    }

    fun setLocale(ctx: Context, locale: String) {
        localDatasource.setLocale(ctx, locale)
    }

    fun getLocale(ctx: Context): String {
        return localDatasource.getLocale(ctx)
    }

    fun setLocaleAutoChanged(ctx: Context, value: Boolean) {
        localDatasource.setLocaleAutoChanged(ctx, value)
    }

    fun isLocaleAutoChanged(ctx: Context): Boolean {
        return localDatasource.isLocaleAutoChanged(ctx)
    }
}