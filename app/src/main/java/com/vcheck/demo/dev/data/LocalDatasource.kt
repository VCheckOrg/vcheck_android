package com.vcheck.demo.dev.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.vcheck.demo.dev.domain.DocTypeData
import java.util.*

class LocalDatasource {

    //TODO think of optimal way of caching single object:
    private var _selectedDocTypeWithData: DocTypeData? = null

    private fun getSharedPreferences(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)

    fun storeVerifToken(ctx: Context, verifToken: String) {
        getSharedPreferences(ctx).edit().putString("verif_token", "Bearer $verifToken").apply()
    }

    fun getVerifToken(ctx: Context): String {
        return getSharedPreferences(ctx).getString("verif_token", "")!!
    }

    fun storeSelectedCountryCode(ctx: Context, countryCode: String) {
        getSharedPreferences(ctx).edit().putString("selected_country_code", countryCode).apply()
    }

    fun getSelectedCountryCode(ctx: Context): String {
        return getSharedPreferences(ctx).getString("selected_country_code", "ua")!!
    }

    fun setSelectedDocTypeWithData(data: DocTypeData) {
        _selectedDocTypeWithData = data
    }

    fun getSelectedDocTypeWithData(): DocTypeData? {
        return _selectedDocTypeWithData
    }

    fun resetCacheOnStartup(ctx: Context) {
        getSharedPreferences(ctx).edit().remove("verif_token").apply()
        getSharedPreferences(ctx).edit().remove("locale").apply()
        getSharedPreferences(ctx).edit().remove("selected_country_code").apply()
        _selectedDocTypeWithData = null
    }
}

//Obsolete logic:

//    fun storeMaxLivenessLocalAttempts(ctx: Context, attempts: Int) {
//        getSharedPreferences(ctx).edit().putInt("max_liveness_attempts", attempts).apply()
//    }
//
//    fun getMaxLivenessLocalAttempts(ctx: Context): Int {
//        return getSharedPreferences(ctx).getInt("max_liveness_attempts", 5)
//    }

//    fun incrementActualLivenessLocalAttempts(ctx: Context) {
//        val current = getSharedPreferences(ctx).getInt("curr_liveness_attempts", 1)
//        getSharedPreferences(ctx).edit().putInt("curr_liveness_attempts", current + 1).apply()
//    }
//
//    fun getActualLivenessLocalAttempts(ctx: Context): Int {
//        return getSharedPreferences(ctx).getInt("curr_liveness_attempts", 1)
//    }