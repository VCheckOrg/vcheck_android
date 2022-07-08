package com.vcheck.demo.dev

import android.app.Activity
import android.content.Intent
import com.vcheck.demo.dev.domain.VerificationClientCreationModel
import com.vcheck.demo.dev.domain.VerificationSchemeType
import com.vcheck.demo.dev.presentation.VCheckStartupActivity
import com.vcheck.demo.dev.util.matchesURL
import java.lang.IllegalArgumentException

object VCheckSDK {

    private var partnerEndCallback: (() -> Unit)? = null

    private var partnerId: Int? = null
    private var partnerSecret: String? = null

    private var verificationType: VerificationSchemeType = VerificationSchemeType.FULL_CHECK
    private var partnerUserId: String? = null
    private var partnerVerificationId: String? = null
    private var customVerificationServiceURL: String? = null
    private var customPartnerServiceURL: String? = null

    private var sessionLifetime: Int? = null

    internal var verificationClientCreationModel: VerificationClientCreationModel? = null

    //TODO add onInitError callback for client (?)
    //TODO add UI properties (colors) adjustments to upper SDK config level
    fun start(partnerActivity: Activity) {

        performPreStartChecks()

        this.verificationClientCreationModel = VerificationClientCreationModel(
            partnerId!!, partnerSecret!!, verificationType, partnerUserId,
            partnerVerificationId, customVerificationServiceURL,
            customPartnerServiceURL, sessionLifetime)

        val intent: Intent?
        try {
            intent = Intent(partnerActivity, VCheckStartupActivity::class.java)
            partnerActivity.startActivity(intent)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun performPreStartChecks() {
        if (partnerEndCallback == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner application's callback function (invoked on SDK flow finish) must be provided")
        }
        if (partnerId == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner ID must be provided by client app")
        }
        if (partnerSecret == null) {
            throw IllegalArgumentException("VCheckSDK - error: partner secret must be provided by client app")
        }
        if (partnerUserId != null && partnerUserId!!.isEmpty()) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, partner user ID must be unique to your service and not empty")
        }
        if (partnerVerificationId != null && partnerVerificationId!!.isEmpty()) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, partner verification ID must be unique to your service and not empty")
        }
        if (customVerificationServiceURL != null && !customVerificationServiceURL!!.matchesURL()) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, custom verification service URL must be valid public URL")
        }
        if (customPartnerServiceURL != null && !customPartnerServiceURL!!.matchesURL()) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, custom partner service URL must be valid public URL")
        }
        if (sessionLifetime != null && sessionLifetime!! < 300) {
            throw IllegalArgumentException("VCheckSDK - error: if provided, custom session lifetime should not be less than 300 seconds")
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

    fun customVerificationServiceURL(url: String): VCheckSDK {
        this.customVerificationServiceURL = url
        return this
    }

    fun customPartnerServiceURL(url: String): VCheckSDK {
        this.customPartnerServiceURL = url
        return this
    }

    fun sessionLifetime(lifetime: Int): VCheckSDK {
        this.sessionLifetime = lifetime
        return this
    }
}