package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.DocTypeData

class LocalDatasource {

    private var _selectedDocTypeWithData: DocTypeData? = null

    private var _livenessMilestonesList: List<String>? = null

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

    fun resetCacheOnStartup() {
        _selectedDocTypeWithData = null
        _livenessMilestonesList = null
    }
}


//
//    private fun getSharedPreferences(ctx: Context): SharedPreferences =
//        ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
//
//    fun storeSelectedCountryCode(ctx: Context, countryCode: String) {
//        getSharedPreferences(ctx).edit().putString("selected_country_code", countryCode).apply()
//    }
//
//    fun getSelectedCountryCode(ctx: Context): String {
//        return getSharedPreferences(ctx).getString("selected_country_code", "ua")!!
//    }
//