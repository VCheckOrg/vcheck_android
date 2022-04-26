package com.vcheck.demo.dev.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val repository = (application as VcheckDemoApp).appContainer.mainRepository

        repository.resetCacheOnStartup(this@StartupActivity)

        startActivity(Intent(this@StartupActivity, MainActivity::class.java))
    }
}