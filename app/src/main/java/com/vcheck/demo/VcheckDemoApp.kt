package com.vcheck.demo

import android.app.Application
import com.vcheck.demo.di.AppContainer

class VcheckDemoApp : Application() {

    val appContainer = AppContainer()
}