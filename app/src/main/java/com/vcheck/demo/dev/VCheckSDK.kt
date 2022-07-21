package com.vcheck.demo.dev

import android.app.Activity
import android.content.Intent
import com.vcheck.demo.dev.domain.VerificationClientCreationModel
import com.vcheck.demo.dev.domain.VerificationSchemeType
import com.vcheck.demo.dev.presentation.VCheckStartupActivity
import com.vcheck.demo.dev.util.isValidHexColor
import java.lang.IllegalArgumentException

object VCheckSDK {

    private var partnerEndCallback: (() -> Unit)? = null

    private var partnerId: Int? = null
    private var partnerSecret: String? = null

    private var verificationType: VerificationSchemeType? = null
    private var partnerUserId: String? = null
    private var partnerVerificationId: String? = null
    private var sessionLifetime: Int? = null

    internal var verificationClientCreationModel: VerificationClientCreationModel? = null

    internal var buttonsColorHex: String? = null
    internal var backgroundPrimaryColorHex: String? = null
    internal var backgroundSecondaryColorHex: String? = null
    internal var backgroundTertiaryColorHex: String? = null
    internal var primaryTextColorHex: String? = null
    internal var secondaryTextColorHex: String? = null
    internal var borderColorHex: String? = null
    private const val wrongColorFormatPickDescr: String = "VCheckSDK - error: if provided, " +
            "custom buttons color should be a valid HEX string (RGB or ARGB). Ex.: '#2A2A2A' or '#abdbe3'"

    //TODO add onInitError callback for client (?)
    fun start(partnerActivity: Activity) {

        performPreStartChecks()

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

    fun onFinish() {
        this.partnerEndCallback?.invoke()
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
        buttonsColorHex = colorHex
        return this
    }

    fun colorBackgroundPrimary(colorHex: String): VCheckSDK {
        backgroundPrimaryColorHex = colorHex
        return this
    }

    fun colorBackgroundSecondary(colorHex: String): VCheckSDK {
        backgroundSecondaryColorHex = colorHex
        return this
    }

    fun colorBackgroundTertiary(colorHex: String): VCheckSDK {
        backgroundTertiaryColorHex = colorHex
        return this
    }

    fun colorTextPrimary(colorHex: String): VCheckSDK {
        primaryTextColorHex = colorHex
        return this
    }

    fun colorTextSecondary(colorHex: String): VCheckSDK {
        secondaryTextColorHex = colorHex
        return this
    }

    fun colorBorders(colorHex: String): VCheckSDK {
        borderColorHex = colorHex
        return this
    }

    fun resetCustomColors() {
        buttonsColorHex = null
        backgroundPrimaryColorHex = null
        backgroundSecondaryColorHex = null
        backgroundTertiaryColorHex = null
        primaryTextColorHex = null
        secondaryTextColorHex = null
        borderColorHex = null
    }


//    private var customVerificationServiceURL: String? = null
//    private var customPartnerServiceURL: String? = null
//    fun customVerificationServiceURL(url: String): VCheckSDK {
//        this.customVerificationServiceURL = url
//        return this
//    }
//
//    fun customPartnerServiceURL(url: String): VCheckSDK {
//        this.customPartnerServiceURL = url
//        return this
//    }
}