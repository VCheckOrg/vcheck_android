package com.vcheck.sdk.core.presentation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ActivityVcheckMainBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.util.VCheckContextUtils


internal class VCheckMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVcheckMainBinding

    private fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.activityMainBackground.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.backArrow.setColorFilter(Color.parseColor(it))
            binding.popSdkTitle.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = VCheckSDK.getSDKLangCode()
        VCheckContextUtils.updateLocale(this@VCheckMainActivity, languageCode)

        binding = ActivityVcheckMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeColorsToCustomIfPresent()

        setHeader()
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo = VCheckSDK.getSDKLangCode()
        val localeUpdatedContext: ContextWrapper =
            VCheckContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun setHeader() {
        binding.logo.isVisible = VCheckSDK.showPartnerLogo

        if (VCheckSDK.showCloseSDKButton) {
            binding.closeSDKBtnHolder.isVisible = true
            binding.closeSDKBtnHolder.setOnClickListener {
                closeSDKFlow()
            }
        } else {
            binding.closeSDKBtnHolder.isVisible = false
        }
    }

    fun closeSDKFlow() {
        (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        val intents = Intent(this@VCheckMainActivity, VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}