package com.vcheck.sdk.core

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.vcheck.sdk.core.data.VCheckSDKConstantsProvider
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.domain.ProviderLogicCase
import java.lang.IllegalArgumentException

object VCheckSDK {

    const val TAG = "VCheckSDK"

    private var partnerEndCallback: (() -> Unit)? = null

    private var onVerificationExpired: (() -> Unit)? = null
    private var isVerifExpired: Boolean = false

    private var verificationToken: String? = null

    private var selectedProvider: Provider? = null
    private var providerLogicCase: ProviderLogicCase? = null
    private var allAvailableProviders: List<Provider>? = null

    // optional; only when provider allows document check stage!
    private var optSelectedCountryCode: String? = null

    private var verificationType: VerificationSchemeType? = null

    private var sdkLanguageCode: String? = null

    private var environment: VCheckEnvironment? = null

    internal var showPartnerLogo: Boolean = false
    internal var showCloseSDKButton: Boolean = true

    internal var designConfig: VCheckDesignConfig? = null

    fun start(partnerActivity: Activity) {

        resetVerification()

        performPreStartChecks()

        val intent: Intent?
        try {
            intent = Intent(partnerActivity, VCheckStartupActivity::class.java)
            partnerActivity.startActivity(intent)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun resetVerification() {
        VCheckDIContainer.mainRepository.resetCache()
        this.optSelectedCountryCode = null
        this.isVerifExpired = false
    }

    private fun performPreStartChecks() {
        if (environment == null) {
            Log.i(TAG, "VCheckSDK - warning: sdk environment is not set by partner developer |" +
                    " see VCheckSDK.environment(env: VCheckEnvironment)")
            environment = VCheckEnvironment.DEV
        }
        if (environment == VCheckEnvironment.DEV) {
            Log.i(TAG, "VCheckSDK - warning: using DEV environment | see VCheckSDK.environment(env: VCheckEnvironment)")
        }
        if (verificationToken == null) {
            throw IllegalArgumentException("VCheckSDK - error: verification token must be provided |" +
                    " see VCheckSDK.verificationToken(token: String)")
        }
        if (verificationType == null) {
            throw IllegalArgumentException("VCheckSDK - error: verification type must be provided |" +
                    " see VCheckSDK.verificationType(type: VerificationSchemeType)")
        }
        if (partnerEndCallback == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner application's callback function " +
                    "(invoked on SDK flow finish) must be provided by partner app |" +
                    " see VCheckSDK.partnerEndCallback(callback: (() -> Unit))")
        }
        if (onVerificationExpired == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner application's onVerificationExpired function " +
                    "(invoked on SDK's current verification expiration case) must be provided by partner app |" +
                    " see VCheckSDK.onVerificationExpired(callback: (() -> Unit))")
        }
        if (sdkLanguageCode == null) {
            Log.w(TAG, "VCheckSDK - warning: SDK language code is not set; using English (en) locale as default. " +
                    "| see VCheckSDK.sdkLanguageCode(langCode: String)")
        }
        if (sdkLanguageCode != null && !VCheckSDKConstantsProvider
                .vcheckSDKAvailableLanguagesList.contains(sdkLanguageCode?.lowercase())) {
            throw IllegalArgumentException("VCheckSDK - error: SDK is not localized with [$sdkLanguageCode] locale yet. " +
                    "You may set one of the next locales: ${VCheckSDKConstantsProvider.vcheckSDKAvailableLanguagesList}, " +
                    "or check out for the recent version of the SDK library")
        }
        if (designConfig == null) {
            Log.w(TAG, "VCheckSDK - warning: No instance of VCheckDesignConfig was passed while " +
                    "initializing SDK | setting VCheck default theme...")
            designConfig = VCheckDesignConfig.fromJSONStr(VCheckSDKConstantsProvider.vcheckDefaultThemeConfig)
        }
    }

    internal fun executePartnerCallback() {
        this.partnerEndCallback?.invoke()
    }

    internal fun executeOnVerificationExpired() {
        this.onVerificationExpired?.invoke()
    }

    internal fun setIsVerificationExpired(isExpired: Boolean) {
        this.isVerifExpired = isExpired
    }

    internal fun isVerificationExpired(): Boolean {
        return this.isVerifExpired
    }

    fun languageCode(langCode: String): VCheckSDK {
        this.sdkLanguageCode = langCode.lowercase()
        return this
    }

    fun onVerificationExpired(callback: (() -> Unit)): VCheckSDK {
        this.onVerificationExpired = callback
        return this
    }

    fun partnerEndCallback(callback: (() -> Unit)): VCheckSDK {
        this.partnerEndCallback = callback
        return this
    }

    fun getVerificationType(): VerificationSchemeType? {
        return this.verificationType
    }

    fun verificationToken(token: String): VCheckSDK {
        this.verificationToken = token
        return this
    }

    fun verificationType(type: VerificationSchemeType): VCheckSDK {
        this.verificationType = type
        return this
    }

    fun getVerificationToken(): String {
        if (verificationToken == null) {
            throw RuntimeException("VCheckSDK - error: verification token is not set!")
        }
        return "Bearer " + verificationToken!!
    }

    fun getSDKLangCode(): String {
        return sdkLanguageCode ?: "en"
    }

    internal fun getEnvironment(): VCheckEnvironment {
        return this.environment ?: VCheckEnvironment.DEV
    }

    internal fun getSelectedProvider(): Provider {
        if (selectedProvider == null) {
            throw RuntimeException("VCheckSDK - error: provider is not set!")
        }
        return selectedProvider!!
    }

    internal fun setSelectedProvider(provider: Provider) {
        this.selectedProvider = provider
    }

    internal fun setAllAvailableProviders(providers: List<Provider>) {
        this.allAvailableProviders = providers
    }

    internal fun getAllAvailableProviders(): List<Provider> {
        if (allAvailableProviders == null) {
            throw RuntimeException("VCheckSDK - error: no providers were cached properly!")
        }
        return allAvailableProviders!!
    }

    internal fun setProviderLogicCase(providerLC: ProviderLogicCase) {
        this.providerLogicCase = providerLC
    }

    internal fun getProviderLogicCase(): ProviderLogicCase {
        if (providerLogicCase == null) {
            throw RuntimeException("VCheckSDK - error: no provider logic case was set!")
        }
        return providerLogicCase!!
    }

    internal fun getOptSelectedCountryCode(): String? {
        return optSelectedCountryCode
    }

    internal fun setOptSelectedCountryCode(code: String) {
        this.optSelectedCountryCode = code
    }

    /// Color customization methods

    fun designConfig(config: VCheckDesignConfig): VCheckSDK {
        this.designConfig = config
        return this
    }

    /// Other public methods for customization

    fun showPartnerLogo(show: Boolean): VCheckSDK {
        this.showPartnerLogo = show
        return this
    }

    fun showCloseSDKButton(show: Boolean): VCheckSDK {
        this.showCloseSDKButton = show
        return this
    }

    fun environment(env: VCheckEnvironment): VCheckSDK {
        this.environment = env
        return this
    }
}