package com.vcheck.demo.dev.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

class VCheckContextUtils(base: Context?) : ContextWrapper(base) {
    companion object {
        fun updateLocale(context: Context, lang: String?): ContextWrapper {
            var ctx: Context = context
            val resources: Resources = ctx.resources
            val configuration: Configuration = resources.configuration
            val localeToSwitchTo = Locale(lang!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(localeToSwitchTo)
                val localeList = LocaleList(localeToSwitchTo)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                ctx = ctx.createConfigurationContext(configuration)
            } else {
                Locale.setDefault(localeToSwitchTo)
                configuration.locale = localeToSwitchTo
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            return VCheckContextUtils(ctx)
        }
    }
}