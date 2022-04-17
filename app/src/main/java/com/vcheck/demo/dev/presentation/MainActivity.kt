package com.vcheck.demo.dev.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.di.AppContainer
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private var wasLocaleSelectedByUser = false
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appContainer = (application as VcheckDemoApp).appContainer

        val languageCode = appContainer.mainRepository.getLocale(applicationContext)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val res = resources
        val config = Configuration(res.configuration)
        config.locale = locale
        res.updateConfiguration(config, res.displayMetrics)

        val langSpinner = findViewById<Spinner>(R.id.lang_spinner)

        val adapter = ArrayAdapter.createFromResource(
            this@MainActivity,
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

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (wasLocaleSelectedByUser) {
            when (position) {
                0 -> {
                    appContainer.mainRepository.setLocale(applicationContext, "uk")
                }
                1 -> {
                    appContainer.mainRepository.setLocale(applicationContext, "en")
                }
                2 -> {
                    appContainer.mainRepository.setLocale(applicationContext, "ru")
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
}

