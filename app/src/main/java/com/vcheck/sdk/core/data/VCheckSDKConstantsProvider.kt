package com.vcheck.sdk.core.data

class VCheckSDKConstantsProvider {

    companion object {

        const val DEV_VERIFICATIONS_SERVICE_URL = "https://test-verification.vycheck.com"
        const val PARTNER_VERIFICATIONS_SERVICE_URL = "https://verification.vycheck.com"

        const val DEV_VERIFICATIONS_API_BASE_URL = "https://test-verification.vycheck.com/api/v1/"
        const val PARTNER_VERIFICATIONS_API_BASE_URL = "https://verification.vycheck.com/api/v1/"

        val vcheckSDKAvailableLanguagesList = listOf<String>(
            "uk",
            "en",
            "ru",
            "pl"
        )
    }
}