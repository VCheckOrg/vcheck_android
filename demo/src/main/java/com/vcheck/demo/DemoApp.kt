package com.vcheck.demo

import android.app.Application
import com.vcheck.demo.AppContainer

class DemoApp: Application() {

    var appContainer: AppContainer = AppContainer()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DemoApp
            private set
    }
}