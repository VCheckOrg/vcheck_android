package com.vcheck.sdk.core.presentation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.util.VCheckContextUtils
import com.vcheck.sdk.core.util.changeStatusBarColor

internal class VCheckStartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vcheck_splash)

        val repository = VCheckDIContainer.mainRepository

        VCheckSDK.backgroundPrimaryColorHex?.let {
            val back = findViewById<ConstraintLayout>(R.id.vcheck_splash_background)
            back.setBackgroundColor(Color.parseColor(it))
            changeStatusBarColor(Color.parseColor(it))
        }

        if (repository.shouldFinishStartupActivity()) {
            repository.setFinishStartupActivity(false)
            repository.resetCache()
            finish()
            if (VCheckSDK.isVerificationExpired()) {
                repository.setFirePartnerCallback(false)
                VCheckSDK.executeOnVerificationExpired()
            } else {
                if (repository.shouldFirePartnerCallback()) {
                    repository.setFirePartnerCallback(false)
                    VCheckSDK.executePartnerCallback()
                }
            }
        } else {
            startActivity(Intent(this@VCheckStartupActivity, VCheckMainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo: String = VCheckSDK.getSDKLangCode()
        val localeUpdatedContext: ContextWrapper =
            VCheckContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}