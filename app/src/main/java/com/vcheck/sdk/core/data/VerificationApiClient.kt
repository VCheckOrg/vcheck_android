package com.vcheck.sdk.core.data

import com.vcheck.sdk.core.domain.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface VerificationApiClient {

    @GET("providers")
    fun getProviders(@Header("Authorization") verifToken: String): Call<ProvidersResponse>

    @PUT("providers/init")
    fun initProvider(@Header("Authorization") verifToken: String,
                     @Body initProviderRequestBody: InitProviderRequestBody): Call<Response<Void>>

    @PUT("verifications/init")
    fun initVerification(@Header("Authorization") verifToken: String): Call<VerificationInitResponse>

    @GET("stages/current")
    fun getCurrentStage(@Header("Authorization") verifToken: String) : Call<StageResponse>

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
        @Part manual: MultipartBody.Part
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
        @Part manual: MultipartBody.Part
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
        @Part country: MultipartBody.Part,
        @Part category: MultipartBody.Part,
        @Part index: MultipartBody.Part
    ) : Call<SegmentationGestureResponse>
}