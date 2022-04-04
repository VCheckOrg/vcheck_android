package com.vcheck.demo.dev.data

import android.content.Context
import android.content.Context.MODE_PRIVATE

class LocalDatasource() {

    fun storeVerifToken(ctx: Context, verifToken: String) {
        val sharedPreferences = ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("verif_token", "Bearer $verifToken").apply()
    }

    fun getVerifToken(ctx: Context): String {
        val sharedPreferences = ctx.getSharedPreferences("vcheck_private_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("verif_token", "")!!
    }
}