package com.vcheck.demo.dev

import android.app.Activity
import android.content.Intent
import com.vcheck.demo.dev.presentation.VCheckStartupActivity

object VCheckSDK {

    private var finishSDKFlowCallback: (() -> Unit)? = null

    fun start(partnerActivity: Activity,
              partnerCallbackOnVerifSuccess: (() -> Unit)) {

        finishSDKFlowCallback = partnerCallbackOnVerifSuccess

        val intent: Intent?
        try {
            intent = Intent(partnerActivity, VCheckStartupActivity::class.java)
            partnerActivity.startActivity(intent)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun onFinish() {
        finishSDKFlowCallback?.invoke()
    }
}