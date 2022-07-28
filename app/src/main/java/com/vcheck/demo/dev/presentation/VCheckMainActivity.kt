package com.vcheck.demo.dev.presentation

import android.content.*
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.ActivityVcheckMainBinding
import com.vcheck.demo.dev.util.VCheckContextUtils


internal class VCheckMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVcheckMainBinding

    private fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.activityMainBackground.setBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = VCheckSDK.getSDKLangCode()
        VCheckContextUtils.updateLocale(this@VCheckMainActivity, languageCode)

        binding = ActivityVcheckMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeColorsToCustomIfPresent()
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


}