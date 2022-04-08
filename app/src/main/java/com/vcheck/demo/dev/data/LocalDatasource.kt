package com.vcheck.demo.dev.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.vcheck.demo.dev.domain.DocTypeData

class LocalDatasource() {

    //TODO think of optimal way of caching single object:
    private lateinit var _selectedDocTypeWithData: DocTypeData

    private fun getSharedPreferences(ctx: Context): SharedPreferences
        = ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)

    fun storeVerifToken(ctx: Context, verifToken: String) {
        getSharedPreferences(ctx).edit().putString("verif_token", "Bearer $verifToken").apply()
    }

    fun getVerifToken(ctx: Context) : String {
        return getSharedPreferences(ctx).getString("verif_token", "")!!
    }

    fun storeSelectedCountryCode(ctx: Context, countryCode: String) {
        getSharedPreferences(ctx).edit().putString("selected_country_code", countryCode).apply()
    }

    fun getSelectedCountryCode(ctx: Context) : String {
        return getSharedPreferences(ctx).getString("selected_country_code", "ua")!!
    }

    fun setSelectedDocTypeWithData(data: DocTypeData) {
        _selectedDocTypeWithData = data
    }

    fun getSelectedDocTypeWithData(): DocTypeData {
        return _selectedDocTypeWithData
    }

    fun storeChosenCountry(ctx: Context, country: String) {
        val sharedPreferences = ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("chosen_country", country).apply()
    }

    fun getChosenCountry(ctx: Context): String {
        val sharedPreferences = ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("chosen_country", "ua")!!
    }
}