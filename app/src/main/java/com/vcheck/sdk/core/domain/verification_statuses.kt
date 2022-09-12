package com.vcheck.sdk.core.domain

class VerificationStatuses {

    companion object {
        const val CREATED = 0
        const val INITIALIZED = 1
        const val WAITING_USER_INTERACTION = 2
        const val WAITING_POSTPROCESSING = 3
        const val IN_POSTPROCESSING = 4
        const val WAITING_MANUAL_CHECK = 5
        const val FINALIZED = 6
    }
}
