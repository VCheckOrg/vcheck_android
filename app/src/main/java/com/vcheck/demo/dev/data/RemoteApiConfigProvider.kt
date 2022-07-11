package com.vcheck.demo.dev.data

class RemoteApiConfigProvider {

    companion object {
        const val VERIFICATIONS_API_BASE_URL = "https://test-verification-new.vycheck.com/api/v1/"
        const val PARTNER_API_BASE_URL = "https://test-partner.vycheck.com/v1/"
        const val DEFAULT_SESSION_LIFETIME = 3600
    }

//    internal fun setVerificationsApiBaseUrl(url: String) {
//        VERIFICATIONS_API_BASE_URL = url
//    }
//
//    internal fun setPartnerApiBaseUrl(url: String) {
//        PARTNER_API_BASE_URL = url
//    }
//
//    internal fun getVerificationsApiBaseUrl(): String {
//        return VERIFICATIONS_API_BASE_URL
//    }
//
//    internal fun getPartnerApiBaseUrl(): String {
//        return PARTNER_API_BASE_URL
//    }
}