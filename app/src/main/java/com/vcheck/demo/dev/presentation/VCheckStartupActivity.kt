package com.vcheck.demo.dev.presentation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.util.ContextUtils

class VCheckStartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vcheck_splash)

        val repository = (application as VCheckSDKApp).appContainer.mainRepository

        repository.resetCacheOnStartup(this@VCheckStartupActivity)

        startActivity(Intent(this@VCheckStartupActivity, VCheckMainActivity::class.java))
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo: String = ContextUtils.getSavedLanguage(newBase)
        Log.d("Ok", "======== attachBaseContext[StartupActivity] LOCALE TO SWITCH TO : $localeToSwitchTo")
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}