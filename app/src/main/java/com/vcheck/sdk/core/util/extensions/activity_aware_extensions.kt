package com.vcheck.sdk.core.util.extensions

import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.BaseClientErrors
import com.vcheck.sdk.core.domain.StageErrorType
import com.vcheck.sdk.core.domain.toTypeIdx
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.util.utils.isColorDark

fun AppCompatActivity.changeStatusBarColor(color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isColorDark(color)
}

fun AppCompatActivity.closeSDKFlow(shouldExecuteEndCallback: Boolean) {
    (VCheckDIContainer).mainRepository.setFirePartnerCallback(shouldExecuteEndCallback)
    (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
    val intents = Intent(this, VCheckStartupActivity::class.java)
    intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intents)
}

fun AppCompatActivity.checkUserInteractionCompletedForResult(errorCode: Int?) {
    if (errorCode == BaseClientErrors.USER_INTERACTED_COMPLETED) {
        (VCheckDIContainer).mainRepository.setFirePartnerCallback(false)
        VCheckSDK.setIsVerificationExpired(true)
        (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        val intents = Intent(this, VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}

fun AppCompatActivity.checkStageErrorForResult(errorCode: Int?, executePartnerCallback: Boolean) {
   if (errorCode != null &&
        errorCode > StageErrorType.VERIFICATION_NOT_INITIALIZED.toTypeIdx()
    ) {
        // e.g.: (errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()
        //    || errorCode == StageObstacleErrorType.VERIFICATION_EXPIRED.toTypeIdx())
        if (executePartnerCallback) {
            (VCheckDIContainer).mainRepository.setFirePartnerCallback(true)
            VCheckSDK.setIsVerificationExpired(false)
            (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        } else {
            (VCheckDIContainer).mainRepository.setFirePartnerCallback(false)
            VCheckSDK.setIsVerificationExpired(true)
            (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        }
        val intents = Intent(this, VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}