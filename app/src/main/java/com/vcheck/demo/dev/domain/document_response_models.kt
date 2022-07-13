package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class DocumentUploadResponse(
    @SerializedName("data")
    val data: DocumentUploadResponseData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class DocumentUploadResponseData(
    @SerializedName("id")
    val id: Int
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
    val docFields: List<DocField>
)

data class DocField(
    @SerializedName("name")
    val name: String,
    @SerializedName("title")
    val title: DocTitle,
    @SerializedName("type")
    val type: String,
    @SerializedName("regex")
    val regex: String? = null
)

data class DocFieldWitOptPreFilledData(
    val name: String,
    val title: DocTitle,
    val type: String,
    val regex: String?,
    var autoParsedValue: String = ""
)

data class DocTitle(
    @SerializedName("uk")
    val ua: String?,
    @SerializedName("en")
    val en: String,
    @SerializedName("ru")
    val ru: String?
)

// --- PRE-PROCESSED DOC

data class PreProcessedDocumentResponse(
    @SerializedName("data")
    val data: PreProcessedDocData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class PreProcessedDocData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: ArrayList<String> = arrayListOf(),
    @SerializedName("is_primary")
    val isPrimary: Boolean,
    @SerializedName("parsed_data")
    val parsedData: ParsedDocFieldsData,
    @SerializedName("status")
    val status: Int,
    @SerializedName("type")
    val type: DocTypeData
)

data class ParsedDocFieldsData(
    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("number")
    var number: String? = null,
    @SerializedName("surname")
    var surname: String? = null
)