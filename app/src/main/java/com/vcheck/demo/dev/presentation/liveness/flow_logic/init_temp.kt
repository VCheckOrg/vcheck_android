package com.vcheck.demo.dev.ported

import java.lang.IllegalArgumentException

val allLivenessActions = arrayOf(
    "Liveness",
    "LivenessEvent",
    "LivenessFailureReason",
    "LivenessError",
    "AlreadyFinalizedError",
)

data class LivenessError(val exception: Exception)

data class AlreadyFinalizedError(val error: LivenessError)

data class ChallengeFrameErrorExceeded(val exception: Exception)

data class EndLiveness(val livenessError: LivenessError)

enum class LivenessEvent {
    START,
    NEXT_GESTURE,
    FAILURE,
    SUCCESS,
    NO_FACE,
    NEW_FACE,
    MULTIPLE_FACES
}

fun livenessEventToStrCode(livenessEvent: LivenessEvent): String {
    return livenessEvent.name.lowercase()
}

enum class LivenessFailureReason {
    FACE_NOT_FOUND,
    MULTIPLE_FACES,
    TIMEOUT,
    FAST_MOVEMENT,
    TOO_DARK
}

fun livenessFailureReasonToStrCode(livenessFailureReason: LivenessFailureReason): String {
    return livenessFailureReason.name.lowercase()
}