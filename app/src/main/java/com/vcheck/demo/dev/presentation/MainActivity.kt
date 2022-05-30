package com.vcheck.demo.dev.presentation

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.util.ContextUtils

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private var wasLocaleSelectedByUser = false
    private lateinit var appContainer: AppContainer
    private var mLangReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = (application as VcheckDemoApp).appContainer

        val languageCode = ContextUtils.getSavedLanguage(this@MainActivity)
        ContextUtils.updateLocale(this@MainActivity, languageCode)

        setContentView(R.layout.activity_main)

        setLangSpinner()

        setupLangReceiver()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (wasLocaleSelectedByUser) {
            when (position) {
                0 -> {
                    ContextUtils.setSavedLanguage(this@MainActivity, "uk")
                    ContextUtils.updateLocale(this@MainActivity, "uk")
                }
                1 -> {
                    ContextUtils.setSavedLanguage(this@MainActivity, "en")
                    ContextUtils.updateLocale(this@MainActivity, "en")
                }
                2 -> {
                    ContextUtils.setSavedLanguage(this@MainActivity, "ru")
                    ContextUtils.updateLocale(this@MainActivity, "ru")
                }
            }
            recreate()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        wasLocaleSelectedByUser = true
        return false
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo: String = ContextUtils.getSavedLanguage(newBase)
        Log.d("Ok", "======== attachBaseContext[MainActivity] LOCALE TO SWITCH TO : $localeToSwitchTo")
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun setupLangReceiver(): BroadcastReceiver? {
        if (mLangReceiver == null) {
            mLangReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("OkHttp", "======== LANG CHANGED VIA RECEIVER")
                    recreate()
                }
            }
            registerReceiver(mLangReceiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
        }
        return mLangReceiver
    }

    private fun setLangSpinner() {

        val languageCode = ContextUtils.getSavedLanguage(this@MainActivity)

        val langSpinner = findViewById<Spinner>(R.id.lang_spinner)

        val adapter = ArrayAdapter.createFromResource(this@MainActivity,
            R.array.languages, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        langSpinner.adapter = adapter
        langSpinner.onItemSelectedListener = this@MainActivity
        langSpinner.setOnTouchListener(this@MainActivity)

        langSpinner.post {
            when (languageCode) {
                "uk" -> {
                    langSpinner.setSelection(0)
                }
                "en" -> {
                    langSpinner.setSelection(1)
                }
                "ru" -> {
                    langSpinner.setSelection(2)
                }
                else -> {
                    langSpinner.setSelection(1)
                }
            }
        }
    }
}