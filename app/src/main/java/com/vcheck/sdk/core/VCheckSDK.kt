package com.vcheck.sdk.core

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.vcheck.sdk.core.data.VCheckSDKConstantsProvider
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.presentation.transferrable_objects.ProviderLogicCase
import com.vcheck.sdk.core.util.isValidHexColor
import java.lang.IllegalArgumentException

object VCheckSDK {

    const val TAG = "VCheckSDK"

    private var partnerEndCallback: (() -> Unit)? = null

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

    internal var buttonsColorHex: String? = null
    internal var backgroundPrimaryColorHex: String? = null
    internal var backgroundSecondaryColorHex: String? = null
    internal var backgroundTertiaryColorHex: String? = null
    internal var primaryTextColorHex: String? = null
    internal var secondaryTextColorHex: String? = null
    internal var borderColorHex: String? = null
    internal var iconsColorHex: String? = null

    private const val wrongColorFormatPickDescr: String = "VCheckSDK - error: if provided, " +
            "custom color should be a valid HEX string (RGB or ARGB). Ex.: '#2A2A2A' or '#abdbe3'"

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
    }

    private fun performPreStartChecks() {
        if (environment == null) {
            Log.i(TAG, "VCheckSDK - warning: sdk environment is not set | see VCheckSDK.environment(env: VCheckEnvironment)")
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
                    "(invoked on SDK flow finish) must be provided by partner app | see VCheckSDK.partnerEndCallback(callback: (() -> Unit))")
        }
        if (sdkLanguageCode == null) {
            Log.w(TAG, "VCheckSDK - warning: sdk language code is not set; using English (en) locale as default. " +
                    "| see VCheckSDK.sdkLanguageCode(langCode: String)")
        }
        if (sdkLanguageCode != null && !VCheckSDKConstantsProvider
                .vcheckSDKAvailableLanguagesList.contains(sdkLanguageCode?.lowercase())) {
            throw IllegalArgumentException("VCheckSDK - error: SDK is not localized with [$sdkLanguageCode] locale yet. " +
                    "You may set one of the next locales: ${VCheckSDKConstantsProvider.vcheckSDKAvailableLanguagesList}, " +
                    "or check out for the recent version of the SDK library")
        }
        if (buttonsColorHex != null && !buttonsColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (backgroundPrimaryColorHex != null && !backgroundPrimaryColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (backgroundSecondaryColorHex != null && !backgroundSecondaryColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (backgroundTertiaryColorHex != null && !backgroundTertiaryColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (primaryTextColorHex != null && !primaryTextColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (secondaryTextColorHex != null && !secondaryTextColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (borderColorHex != null && !borderColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
        if (iconsColorHex != null && !iconsColorHex!!.isValidHexColor()) {
            throw IllegalArgumentException(wrongColorFormatPickDescr)
        }
    }

    internal fun executePartnerCallback() {
        this.partnerEndCallback?.invoke()
    }

    fun languageCode(langCode: String): VCheckSDK {
        this.sdkLanguageCode = langCode.lowercase()
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

    /// Color customization methods

    fun colorActionButtons(colorHex: String): VCheckSDK {
        this.buttonsColorHex = colorHex
        return this
    }

    fun colorBackgroundPrimary(colorHex: String): VCheckSDK {
        this.backgroundPrimaryColorHex = colorHex
        return this
    }

    fun colorBackgroundSecondary(colorHex: String): VCheckSDK {
        this.backgroundSecondaryColorHex = colorHex
        return this
    }

    fun colorBackgroundTertiary(colorHex: String): VCheckSDK {
        this.backgroundTertiaryColorHex = colorHex
        return this
    }

    fun colorTextPrimary(colorHex: String): VCheckSDK {
        this.primaryTextColorHex = colorHex
        return this
    }

    fun colorTextSecondary(colorHex: String): VCheckSDK {
        this.secondaryTextColorHex = colorHex
        return this
    }

    fun colorBorders(colorHex: String): VCheckSDK {
        this.borderColorHex = colorHex
        return this
    }

    fun colorIcons(colorHex: String): VCheckSDK {
        this.iconsColorHex = colorHex
        return this
    }

    fun resetCustomColors() {
        this.buttonsColorHex = null
        this.backgroundPrimaryColorHex = null
        this.backgroundSecondaryColorHex = null
        this.backgroundTertiaryColorHex = null
        this.primaryTextColorHex = null
        this.secondaryTextColorHex = null
        this.borderColorHex = null
        this.iconsColorHex = null
    }

    /// Internal caching functions

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

    //TODO test with optional country!
    internal fun getOptSelectedCountryCode(): String? {
        return optSelectedCountryCode
    }

    internal fun setOptSelectedCountryCode(code: String) {
        this.optSelectedCountryCode = code
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