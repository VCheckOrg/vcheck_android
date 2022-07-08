package com.vcheck.demo.dev.data

class RemoteApiConfigProvider {

    private var VERIFICATIONS_API_BASE_URL = "https://test-verification.vycheck.com/api/v1/"
    private var PARTNER_API_BASE_URL = "https://test-partner.vycheck.com/api/v1/"

    companion object {
        const val DEFAULT_SESSION_LIFETIME = 3600
    }

    internal fun setVerificationsApiBaseUrl(url: String) {
        VERIFICATIONS_API_BASE_URL = url
    }

    internal fun setPartnerApiBaseUrl(url: String) {
        PARTNER_API_BASE_URL = url
    }

    internal fun getVerificationsApiBaseUrl(): String {
        return VERIFICATIONS_API_BASE_URL
    }

    internal fun getPartnerApiBaseUrl(): String {
        return PARTNER_API_BASE_URL
    }
}