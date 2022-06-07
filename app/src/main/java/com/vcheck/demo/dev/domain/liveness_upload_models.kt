package com.vcheck.demo.dev.domain

import com.google.gson.annotations.SerializedName

data class LivenessUploadResponse(
    @SerializedName("data")
    val data: LivenessUploadResponseData,
    @SerializedName("error_code")
    var errorCode: Int = 0,
    @SerializedName("message")
    var message: String = ""
)

data class LivenessUploadResponseData(
    @SerializedName("is_final")
    val isFinal: Boolean,
    @SerializedName("status")
    val status: Int,
    @SerializedName("reason")
    val reason: String? //not empty/null if status code corresponds to FAIL
)


enum class LivenessChallengeStatus {
    INITIALIZED,
    RUNNING,
    SUCCESS,
    FAIL
}

fun statusCodeToLivenessChallengeStatus(code: Int): LivenessChallengeStatus {
    return when(code) {
        0 -> LivenessChallengeStatus.INITIALIZED
        1 -> LivenessChallengeStatus.RUNNING
        2 -> LivenessChallengeStatus.SUCCESS
        3 -> LivenessChallengeStatus.FAIL
        else -> LivenessChallengeStatus.FAIL
    }
}

fun livenessChallengeStatusToCode(livenessChallengeStatus: LivenessChallengeStatus): Int {
    return when (livenessChallengeStatus) {
        LivenessChallengeStatus.INITIALIZED ->  0
        LivenessChallengeStatus.RUNNING -> 1
        LivenessChallengeStatus.SUCCESS -> 2
        LivenessChallengeStatus.FAIL -> 3
    }
}

enum class LivenessFailureReason {
    FACE_NOT_FOUND,
    MULTIPLE_FACES,
    FAST_MOVEMENT,
    TOO_DARK,
    INVALID_MOVEMENTS,
    UNKNOWN;

    companion object {
        fun from(type: String?): LivenessFailureReason = values().find {
            it.name.lowercase() == type?.lowercase() } ?: UNKNOWN
    }
}

fun strCodeToLivenessFailureReason(strCode: String): LivenessFailureReason {
    return LivenessFailureReason.from(strCode)
}

fun livenessFailureReasonToStrCode(r: LivenessFailureReason): String {
    return r.name.lowercase()
}

//Reasons:
//FACE_NOT_FOUND = "face_not_found"
//MULTIPLE_FACES = "multiple_faces"
//TIMEOUT = "timeout"
//FAST_MOVEMENT = "fast_movement"
//TOO_DARK = "too_dark"
//DISCONNECTED = "disconnected"
//NOT_SAME_PERSON = "not_same_person"
//INVALID_MOVEMENTS = "invalid_movements"

// Not used in mobile:
//TIMEOUT,
//DISCONNECTED,
//NOT_SAME_PERSON,