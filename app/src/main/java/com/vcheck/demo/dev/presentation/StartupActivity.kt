package com.vcheck.demo.dev.presentation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.util.ContextUtils

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val repository = (application as VcheckDemoApp).appContainer.mainRepository

        repository.resetCacheOnStartup(this@StartupActivity)

        startActivity(Intent(this@StartupActivity, MainActivity::class.java))
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo: String = ContextUtils.getSavedLanguage(newBase)
        Log.d("Ok", "======== attachBaseContext[StartupActivity] LOCALE TO SWITCH TO : $localeToSwitchTo")
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}