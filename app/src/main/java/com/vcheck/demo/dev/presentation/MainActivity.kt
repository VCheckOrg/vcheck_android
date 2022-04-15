package com.vcheck.demo.dev.presentation

import android.annotation.SuppressLint
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

    private var userSelect = false
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appContainer = (application as VcheckDemoApp).appContainer

        val code = appContainer.mainRepository.getLocale(applicationContext)
        val locale = Locale(code)
        Locale.setDefault(locale)
        val res = resources
        val config = Configuration(res.configuration)
        config.locale = locale
        res.updateConfiguration(config, res.displayMetrics)

        val langSpinner = findViewById<Spinner>(R.id.lang_spinner)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        langSpinner.adapter = adapter;
        langSpinner.onItemSelectedListener = this;
        langSpinner.setOnTouchListener(this)

        when (code) {
            "uk" -> {
                langSpinner.setSelection(0)
            }
            "en" -> {
                langSpinner.setSelection(1)
            }
            "ru" -> {
                langSpinner.setSelection(2)
            }
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (userSelect) {
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
        userSelect = true;
        return false
    }
}

