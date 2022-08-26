package com.vcheck.sdk.core.data

class VCheckSDKConstantsProvider {

    companion object {

        const val VERIFICATIONS_API_BASE_URL = "https://test-verification.vycheck.com/api/v1/"

        val vcheckSDKAvailableLanguagesList = listOf<String>(
            "uk",
            "en",
            "ru",
            "pl"
        )
    }
}