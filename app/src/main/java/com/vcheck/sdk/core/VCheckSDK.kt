package com.vcheck.sdk.core

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.IntentCompat
import com.vcheck.sdk.core.data.VCheckSDKConstantsProvider
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.util.isValidHexColor
import java.lang.IllegalArgumentException

object VCheckSDK {

    const val TAG = "VCheckSDK"

    private var partnerEndCallback: (() -> Unit)? = null

    internal var partnerActivityClass: Class<Activity>? = null

    private var partnerId: Int? = null
    private var partnerSecret: String? = null

    private var verificationToken: String? = null
    private var verificationId: Int? = null

    private var selectedCountryCode: String? = null

    private var verificationType: VerificationSchemeType? = null
    private var partnerUserId: String? = null
    private var partnerVerificationId: String? = null
    private var sessionLifetime: Int? = null

    internal var verificationClientCreationModel: VerificationClientCreationModel? = null

    private var sdkLanguageCode: String? = null

    internal var buttonsColorHex: String? = null
    internal var backgroundPrimaryColorHex: String? = null
    internal var backgroundSecondaryColorHex: String? = null
    internal var backgroundTertiaryColorHex: String? = null
    internal var primaryTextColorHex: String? = null
    internal var secondaryTextColorHex: String? = null
    internal var borderColorHex: String? = null
    private const val wrongColorFormatPickDescr: String = "VCheckSDK - error: if provided, " +
            "custom buttons color should be a valid HEX string (RGB or ARGB). Ex.: '#2A2A2A' or '#abdbe3'"

    fun start(partnerActivity: Activity) {

        resetVerification()

        performPreStartChecks()

        this.partnerActivityClass = partnerActivity.javaClass

        this.verificationClientCreationModel = VerificationClientCreationModel(
            partnerId!!, partnerSecret!!, verificationType!!, partnerUserId,
            partnerVerificationId, sessionLifetime)

        val intent: Intent?
        try {
            intent = Intent(partnerActivity, VCheckStartupActivity::class.java)
            partnerActivity.startActivity(intent)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun resetVerification() {
        this.verificationToken = null
        this.verificationId = null
        this.selectedCountryCode = null
    }

    private fun performPreStartChecks() {
        if (verificationType == null) {
            throw IllegalArgumentException("VCheckSDK - error: verification type must be provided |" +
                    " see VCheckSDK.verificationType(type: VerificationSchemeType)")
        }
        if (partnerEndCallback == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner application's callback function " +
                    "(invoked on SDK flow finish) must be provided by partner app | see VCheckSDK.partnerEndCallback(callback: (() -> Unit))")
        }
        if (partnerId == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner ID must be provided | see VCheckSDK.partnerId(id: Int)")
        }
        if (partnerSecret == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner secret must be provided | see VCheckSDK.partnerSecret(secret: String)")
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
        if (partnerUserId != null && partnerUserId!!.isEmpty()) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, partner user ID must be unique to your service and not empty")
        }
        if (partnerVerificationId != null && partnerVerificationId!!.isEmpty()) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, partner verification ID must be unique to your service and not empty")
        }
        if (sessionLifetime != null && sessionLifetime!! < 300) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, custom session lifetime should not be less than 300 seconds")
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
    }

    fun onApplicationFinish() {
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

    fun partnerId(id: Int): VCheckSDK {
        this.partnerId = id
        return this
    }

    fun partnerSecret(secret: String): VCheckSDK {
        this.partnerSecret = secret
        return this
    }

    fun verificationType(type: VerificationSchemeType): VCheckSDK {
        this.verificationType = type
        return this
    }

    fun partnerUserId(puid: String): VCheckSDK {
        this.partnerUserId = puid
        return this
    }

    fun partnerVerificationId(pverid: String): VCheckSDK {
        this.partnerVerificationId = pverid
        return this
    }

    fun sessionLifetime(lifetime: Int): VCheckSDK {
        this.sessionLifetime = lifetime
        return this
    }

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

    fun resetCustomColors() {
        this.buttonsColorHex = null
        this.backgroundPrimaryColorHex = null
        this.backgroundSecondaryColorHex = null
        this.backgroundTertiaryColorHex = null
        this.primaryTextColorHex = null
        this.secondaryTextColorHex = null
        this.borderColorHex = null
    }

    fun checkFinalVerificationStatus(): VerificationResult {
        val call = VCheckDIContainer.mainRepository
            .checkFinalVerificationStatus(getVerificationId(),
            this.partnerId!!, this.partnerSecret!!)
        return if (call != null) {
            val response = call.execute()
            if (response.isSuccessful && response.body() != null) {
                val bodyDeserialized: FinalVerifCheckResponseModel = response.body() as FinalVerifCheckResponseModel
                val data = bodyDeserialized.data
                VerificationResult(
                    isVerificationFinalizedAndSuccessful(data),
                    isVerificationFinalizedAndFailed(data),
                    isVerificationWaitingForManualCheck(data),
                    data.status, data.scheme, data.createdAt, data.finalizedAt, data.rejectionReasons)
            } else getErrorVerificationResult()
        } else getErrorVerificationResult()
    }

    private fun isVerificationFinalizedAndSuccessful(data: FinalVerifCheckResponseData): Boolean {
        return (data.status.lowercase() == "finalized" && data.isSuccess == true)
    }

    private fun isVerificationFinalizedAndFailed(data: FinalVerifCheckResponseData): Boolean {
        return (data.status.lowercase() == "finalized" && data.isSuccess == false)
    }

    private fun isVerificationWaitingForManualCheck(data: FinalVerifCheckResponseData): Boolean {
        return data.status.lowercase() == "waiting_manual_check"
    }

    private fun getErrorVerificationResult(): VerificationResult {
        return VerificationResult(
            isFinalizedAndSuccessful = false, isFinalizedAndFailed = false,
            isWaitingForManualCheck = false, status = "sdk_client_error",
            scheme = this.verificationType!!.toStringRepresentation(),
            createdAt = null, finalizedAt = null, rejectionReasons = null)
    }

    internal fun setVerificationToken(token: String) {
        this.verificationToken = "Bearer $token"
    }

    internal fun getVerificationToken(): String {
        if (verificationToken == null) {
            throw RuntimeException("VCheckSDK - error: verification token is not set!")
        }
        return verificationToken ?: ""
    }

    internal fun setVerificationId(id: Int) {
        this.verificationId = id
    }

    private fun getVerificationId(): Int {
        if (verificationId == null) {
            throw RuntimeException("VCheckSDK - error: verification id not set!")
        }
        return verificationId ?: -1
    }

    fun getSDKLangCode(): String {
        return sdkLanguageCode ?: "en"
    }

    internal fun getSelectedCountryCode(): String {
        return selectedCountryCode ?: "ua"
    }

    internal fun setSelectedCountryCode(code: String) {
        this.selectedCountryCode = code
    }
}