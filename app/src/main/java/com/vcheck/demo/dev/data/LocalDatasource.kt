package com.vcheck.demo.dev.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.vcheck.demo.dev.domain.DocTypeData

class LocalDatasource {

    //TODO make optimal way of caching single object:
    private var _selectedDocTypeWithData: DocTypeData? = null

    private var _livenessMilestonesList: List<String>? = null

    private fun getSharedPreferences(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)

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

    fun setLivenessMilestonesList(list: List<String>) {
        _livenessMilestonesList = list
    }

    fun getLivenessMilestonesList(): List<String>? {
        return _livenessMilestonesList
    }

    fun resetCacheOnStartup(ctx: Context) {
        getSharedPreferences(ctx).edit().remove("verif_token").apply()
        getSharedPreferences(ctx).edit().remove("locale").apply()
        getSharedPreferences(ctx).edit().remove("selected_country_code").apply()
        _selectedDocTypeWithData = null
        _livenessMilestonesList = null
    }
}


//fun storeVerifToken(ctx: Context, verifToken: String) {
//    getSharedPreferences(ctx).edit().putString("verif_token", "Bearer $verifToken").apply()
//}
//
//fun getVerifToken(ctx: Context): String {
//    return getSharedPreferences(ctx).getString("verif_token", "")!!
//}
