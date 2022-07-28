package com.vcheck.demo.dev.data

class VCheckSDKConstantsProvider {

    companion object {

        const val VERIFICATIONS_API_BASE_URL = "https://test-verification-new.vycheck.com/api/v1/"
        const val PARTNER_API_BASE_URL = "https://test-partner.vycheck.com/v1/"

        const val DEFAULT_SESSION_LIFETIME = 3600

        val vcheckSDKAvailableLanguagesList = listOf<String>(
            "uk",
            "en",
            "ru"
        )
    }
}