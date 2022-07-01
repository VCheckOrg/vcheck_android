package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class StageResponse(
    @SerializedName("data")
    val data: StageResponseData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class StageResponseData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("config")
    val config: Any? = null,
    @SerializedName("primary_document_id") // for DOCUMENT UPLOAD stage only
    val primaryDocId: Int? = null,
    @SerializedName("uploaded_document_id") // for DOCUMENT UPLOAD stage only
    val uploadedDocId: Int? = null
)

enum class StageObstacleErrorType {
    VERIFICATION_NOT_INITIALIZED,
    USER_INTERACTED_COMPLETED
}

fun StageObstacleErrorType.toTypeIdx(): Int {
    return when(this) {
        StageObstacleErrorType.VERIFICATION_NOT_INITIALIZED -> 0
        StageObstacleErrorType.USER_INTERACTED_COMPLETED -> 1
    }
}

fun stageObstacleTypeIdxToError(stageObstacleTypeIdx: Int): StageObstacleErrorType {
    return when(stageObstacleTypeIdx) {
        0 -> StageObstacleErrorType.VERIFICATION_NOT_INITIALIZED
        1 -> StageObstacleErrorType.USER_INTERACTED_COMPLETED
        else -> StageObstacleErrorType.USER_INTERACTED_COMPLETED //!
    }
}

enum class StageType {
    DOCUMENT_UPLOAD,// = 0
    LIVENESS_CHALLENGE// = 1
    //IDENTITY_VERIFICATION = 2 - should not interact with front-end
}

fun StageType.toTypeIdx(): Int {
    return when(this) {
        StageType.DOCUMENT_UPLOAD -> 0
        StageType.LIVENESS_CHALLENGE -> 1
    }
}

fun stageIdxToType(stageIdx: Int): StageType {
    return when(stageIdx) {
        0 -> StageType.DOCUMENT_UPLOAD
        1 -> StageType.LIVENESS_CHALLENGE
        else -> StageType.DOCUMENT_UPLOAD
    }
}