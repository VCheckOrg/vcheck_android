package com.vcheck.demo.dev.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.util.setLangSpinner
import com.vcheck.demo.dev.util.setLocale


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private var wasLocaleSelectedByUser = false
    private lateinit var appContainer: AppContainer
    private var mLangReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = (application as VcheckDemoApp).appContainer
        setLocale(appContainer)

        setContentView(R.layout.activity_main)

        setLangSpinner(appContainer)

        setupLangReceiver()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (wasLocaleSelectedByUser) {
            when (position) {
                0 -> appContainer.mainRepository.setLocale(applicationContext, "uk")
                1 -> appContainer.mainRepository.setLocale(applicationContext, "en")
                2 -> appContainer.mainRepository.setLocale(applicationContext, "ru")
            }
            recreate()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        wasLocaleSelectedByUser = true
        return false
    }

    //TODO: locale setup/change not working on 6.0.1+ !
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode: Int = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    private fun setupLangReceiver(): BroadcastReceiver? {
        if (mLangReceiver == null) {
            mLangReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("OkHttp", "======== LANG CHANGED")
                    recreate()
                }
            }
            registerReceiver(mLangReceiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
        }
        return mLangReceiver
    }
}

