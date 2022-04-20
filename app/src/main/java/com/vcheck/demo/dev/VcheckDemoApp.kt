package com.vcheck.demo.dev

import android.app.Application
import com.vcheck.demo.dev.di.AppContainer

class VcheckDemoApp : Application() {

    var appContainer: AppContainer = AppContainer(this)
}