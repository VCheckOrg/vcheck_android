package com.vcheck.demo.dev

import android.app.Application
import android.content.res.Configuration
import com.vcheck.demo.dev.di.AppContainer
import java.util.*

class VcheckDemoApp : Application() {

    var appContainer: AppContainer = AppContainer(this)

}