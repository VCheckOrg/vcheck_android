package com.vcheck.demo.dev

import android.app.Application
import com.vcheck.demo.dev.di.AppContainer

internal class VCheckSDKApp : Application() {

    var appContainer: AppContainer = AppContainer(this)
}