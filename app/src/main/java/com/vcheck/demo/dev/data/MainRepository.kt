package com.vcheck.demo.dev.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody

class MainRepository(private val remoteDatasource : RemoteDatasource, private val localDatasource: LocalDatasource) {

//  fun createVerificationRequest(verificationRequestBody: CreateVerificationRequestBody): CreateVerificationAttemptResponse
//        = remoteData.createVerificationRequest(verificationRequestBody)

    fun createTestVerificationRequest(): MutableLiveData<Resource<CreateVerificationAttemptResponse>> =
        remoteDatasource.createVerificationRequest(CreateVerificationRequestBody())

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

    fun getCountryAvailableDocTypeInfo(verifToken: String, countryCode: String)
        : MutableLiveData<Resource<DocumentTypesForCountryResponse>> {
        return remoteDatasource.getCountryAvailableDocTypeInfo(verifToken, countryCode)
    }

    fun uploadVerificationDocument(
        token: String,
        image: MultipartBody.Part
    ): MutableLiveData<Resource<DocumentUploadResponse>> {
        return if (token.isNotEmpty()) {
            remoteDatasource.uploadVerificationDocument(token, DocumentUploadRequestBody(), image)
        } else MutableLiveData(Resource.error(ApiError("No token available!")))
    }

    fun storeVerifToken(ctx: Context, verifToken: String) {
        localDatasource.storeVerifToken(ctx, verifToken)
    }

    fun getVerifToken(ctx: Context) : String {
        return localDatasource.getVerifToken(ctx)
    }

    fun storeSelectedCountryCode(ctx: Context, countryCode: String) {
        localDatasource.storeSelectedCountryCode(ctx, countryCode)
    }

    fun getSelectedCountryCode(ctx: Context) : String {
        return localDatasource.getSelectedCountryCode(ctx)
    }

    fun setSelectedDocTypeWithData(data: DocTypeData) {
        localDatasource.setSelectedDocTypeWithData(data)
    }

    fun getSelectedDocTypeWithData(): DocTypeData {
        return localDatasource.getSelectedDocTypeWithData()
    }
}