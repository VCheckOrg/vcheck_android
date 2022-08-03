package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface VerificationApiClient {

    @PUT("verifications/init")
    fun initVerification(@Header("Authorization") verifToken: String): Call<VerificationInitResponse>

    @GET("stages/current")
    fun getCurrentStage(@Header("Authorization") verifToken: String) : Call<StageResponse>

    @GET("documents/countries")
    fun getCountries(@Header("Authorization") verifToken: String): Call<CountriesResponse>

    @GET("documents/types")
    fun getCountryAvailableDocTypeInfo(
        @Header("Authorization") verifToken: String,
        @Query("country") countryCode: String
    ): Call<DocumentTypesForCountryResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents/upload")
    fun uploadVerificationDocumentsForOnePage(
        @Header("Authorization") verifToken: String,
        @Part photo1: MultipartBody.Part,
        @Part country: MultipartBody.Part,
        @Part category: MultipartBody.Part,
    ): Call<DocumentUploadResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents/upload")
    fun uploadVerificationDocumentsForTwoPages(
        @Header("Authorization") verifToken: String,
        @Part photo1: MultipartBody.Part,
        @Part photo2: MultipartBody.Part,
        @Part country: MultipartBody.Part,
        @Part category: MultipartBody.Part,
    ): Call<DocumentUploadResponse>

    @GET("documents/{document}/info")
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

    @Headers("multipart: true")
    @Multipart
    @POST("liveness_challenges/gesture")
    fun sendLivenessGestureAttempt(
        @Header("Authorization") verifToken: String,
        @Part image: MultipartBody.Part,
        @Part gesture: MultipartBody.Part
    ) : Call<LivenessGestureResponse>

    @Headers("multipart: true")
    @Multipart
    @POST("documents/inspect")
    fun sendSegmentationDocAttempt(
        @Header("Authorization") verifToken: String,
        @Part image: MultipartBody.Part,
        @Part country: String,
        @Part category: String,
        @Part index: String
    ) : Call<SegmentationGestureResponse>

    /*
    country - страна документа
    category - категория документа (PASSPORT = 0, FOREIGN_PASSPORT = 1, ID_CARD = 2,)
    index - индекс странички от 0
    image (jpeg) - фото документа с применением маски
     */
}