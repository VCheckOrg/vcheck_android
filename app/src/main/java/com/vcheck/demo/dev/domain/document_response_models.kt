package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class DocumentUploadResponse(
    val data: DocumentUploadResponseData
)

data class DocumentUploadResponseData(
    @SerializedName("status")
    val status: Int,
    @SerializedName("document")
    val document: Int
)

data class PreProcessedDocumentResponse(
    @SerializedName("data")
    val data: DocTypeData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class DocumentTypesForCountryResponse(
    @SerializedName("data")
    val data: List<DocTypeData>,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class DocTypeData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("country")
    val country: String,
    @SerializedName("category")
    val category: Int,
    @SerializedName("min_pages_count")
    val minPagesCount: Int,
    @SerializedName("max_pages_count")
    val maxPagesCount: Int,
    @SerializedName("auto")
    val auto: Boolean,
    @SerializedName("fields")
    val fields: List<DocField>
)

data class DocField(
    @SerializedName("name")
    val name: String,
    @SerializedName("title")
    val title: DocTitle,
    @SerializedName("type")
    val type: String,
    @SerializedName("regex")
    val regex: String
)

data class DocTitle(
    @SerializedName("eng")
    val en: String,
    @SerializedName("ru")
    val ru: String?
)