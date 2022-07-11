package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface VerificationApiClient {

    /*
    https://test-verification-new.vycheck.com/api/v1/ - verification_api (TEST)
    https://test-partner.vycheck.com/api/v1/ - partner_api
    первая правда может поменятся, планируем убрать new как снесем старый тест

    partner_api:
    /verifications

    verification_api:
    /verifications/init
    /stages/current
    /documents/countries
    /documents/types
    /documents/upload
    /documents/<int:verification_document_id>/info
    /documents/<int:verification_document_id>/confirm
    /liveness_challenges
     */

    @PUT("verifications/init")
    fun initVerification(@Header("Authorization") verifToken: String): Call<VerificationInitResponse>

    @GET("documents/countries")
    fun getCountries(@Header("Authorization") verifToken: String): Call<CountriesResponse>

    @GET("documents/types")
    fun getCountryAvailableDocTypeInfo(
        @Header("Authorization") verifToken: String,
        @Query("country") countryCode: String //TODO test!
    ): Call<DocumentTypesForCountryResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents/upload")
    fun uploadVerificationDocumentsForOnePage(
        @Header("Authorization") verifToken: String,
        @Part photo1: MultipartBody.Part,
        @Part country: MultipartBody.Part,
        @Part document_type: MultipartBody.Part, // TODO rename to category = fields.Integer()
    ): Call<DocumentUploadResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents/upload")
    fun uploadVerificationDocumentsForTwoPages(
        @Header("Authorization") verifToken: String,
        @Part photo1: MultipartBody.Part,
        @Part photo2: MultipartBody.Part,
        @Part country: MultipartBody.Part,
        @Part document_type: MultipartBody.Part, // TODO rename to category = fields.Integer()
    ): Call<DocumentUploadResponse>

    @GET("documents/{document}/info") //TODO: change to GET documents/{document}/info
    fun getDocumentInfo(
        @Header("Authorization") verifToken: String,
        @Path("document") docId: Int
    ): Call<PreProcessedDocumentResponse>

    @PUT("documents/{document}/confirm")
    fun updateAndConfirmDocInfo(
        @Header("Authorization") verifToken: String,
        @Path("document") docId: Int,
        @Body parsedDocFieldsData: DocUserDataRequestBody
    ): Call<Response<Void>>

    @GET("timestamp")
    fun getServiceTimestamp() : Call<String>

    @Headers("multipart: true")
    @Multipart
    @POST("liveness_challenges")
    fun uploadLivenessVideo(
        @Header("Authorization") verifToken: String,
        @Part video: MultipartBody.Part
    ) : Call<LivenessUploadResponse>

    @GET("stages/current")
    fun getCurrentStage(
        @Header("Authorization") verifToken: String,
    ) : Call<StageResponse>
}