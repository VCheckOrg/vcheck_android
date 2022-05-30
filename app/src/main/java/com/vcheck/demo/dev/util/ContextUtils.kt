package com.vcheck.demo.dev.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import java.util.*

class ContextUtils(base: Context?) : ContextWrapper(base) {
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
            return ContextUtils(ctx)
        }

        fun getSavedLanguage(context: Context): String {
            val preferences = context.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
            Log.d("OkHttp","========================= DEFAULT LOCALE: ${Locale.getDefault().language}")
            val defaultLocale = when(Locale.getDefault().language)  {
                "uk" -> "uk"
                "ru" -> "ru"
                else -> "en"
            }
            return preferences.getString("locale", defaultLocale)!!
        }

        fun setSavedLanguage(context: Context, lang: String?) {
            val preferences = context.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
            preferences.edit().putString("locale", lang).apply()
        }
    }
}