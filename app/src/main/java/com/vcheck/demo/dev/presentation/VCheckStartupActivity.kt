package com.vcheck.demo.dev.presentation

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.util.ContextUtils

internal class VCheckStartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vcheck_splash)

        val repository = VCheckSDKApp.instance.appContainer.mainRepository

        repository.resetCacheOnStartup(this@VCheckStartupActivity)

        startActivity(Intent(this@VCheckStartupActivity, VCheckMainActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo: String = ContextUtils.getSavedLanguage(newBase)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}