package com.vcheck.demo.dev.util

import android.content.res.Configuration
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.presentation.MainActivity
import java.util.*

fun MainActivity.setLocale(appContainer: AppContainer) {
    val languageCode = appContainer.mainRepository.getLocale(applicationContext)

    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val res = resources
    val config = Configuration(res.configuration)
    config.locale = locale
    res.updateConfiguration(config, res.displayMetrics)

    Log.d("OkHttp", "UPDATED DEFAULT LOCALE TO ${locale.language}")
}

fun MainActivity.setLangSpinner(appContainer: AppContainer) {

    val languageCode = appContainer.mainRepository.getLocale(applicationContext)

    val langSpinner = findViewById<Spinner>(R.id.lang_spinner)

    val adapter = ArrayAdapter.createFromResource(
        this,
        R.array.languages, android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    langSpinner.adapter = adapter
    langSpinner.onItemSelectedListener = this
    langSpinner.setOnTouchListener(this)

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
