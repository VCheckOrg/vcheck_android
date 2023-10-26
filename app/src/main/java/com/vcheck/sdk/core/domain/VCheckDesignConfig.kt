package com.vcheck.sdk.core.domain

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.vcheck.sdk.core.VCheckSDK

data class VCheckDesignConfig (
    @SerializedName("primary")
    var primary: String? = null,
    @SerializedName("primaryHover")
    var primaryHover: String? = null,
    @SerializedName("primaryActive")
    var primaryActive: String? = null,
    @SerializedName("primaryContent")
    var primaryContent: String? = null,
    @SerializedName("primaryBg")
    var primaryBg: String? = null,
    @SerializedName("accent")
    var accent: String? = null,
    @SerializedName("accentHover")
    var accentHover: String? = null,
    @SerializedName("accentActive")
    var accentActive: String? = null,
    @SerializedName("accentContent")
    var accentContent: String? = null,
    @SerializedName("accentBg")
    var accentBg: String? = null,
    @SerializedName("neutral")
    var neutral: String? = null,
    @SerializedName("neutralHover")
    var neutralHover: String? = null,
    @SerializedName("neutralActive")
    var neutralActive: String? = null,
    @SerializedName("neutralContent")
    var neutralContent: String? = null,
    @SerializedName("success")
    var success: String? = null,
    @SerializedName("successHover")
    var successHover: String? = null,
    @SerializedName("successActive")
    var successActive: String? = null,
    @SerializedName("successBg")
    var successBg: String? = null,
    @SerializedName("successContent")
    var successContent: String? = null,
    @SerializedName("error")
    var error: String? = null,
    @SerializedName("errorHover")
    var errorHover: String? = null,
    @SerializedName("errorActive")
    var errorActive: String? = null,
    @SerializedName("errorBg")
    var errorBg: String? = null,
    @SerializedName("errorContent")
    var errorContent: String? = null,
    @SerializedName("warning")
    var warning: String? = null,
    @SerializedName("warningHover")
    var warningHover: String? = null,
    @SerializedName("warningActive")
    var warningActive: String? = null,
    @SerializedName("warningBg")
    var warningBg: String? = null,
    @SerializedName("warningContent")
    var warningContent: String? = null,
    @SerializedName("base")
    var base: String? = null,
    @SerializedName("base_100")
    var base100: String? = null,
    @SerializedName("base_200")
    var base200: String? = null,
    @SerializedName("base_300")
    var base300: String? = null,
    @SerializedName("base_400")
    var base400: String? = null,
    @SerializedName("base_500")
    var base500: String? = null,
    @SerializedName("baseContent")
    var baseContent: String? = null,
    @SerializedName("baseSecondaryContent")
    var baseSecondaryContent : String? = null,
    @SerializedName("disabled")
    var disabled: String? = null,
    @SerializedName("disabledContent")
    var disabledContent: String? = null
) {

    companion object {
        @JvmStatic
        fun fromJsonStr(rawJsonStr: String) : VCheckDesignConfig {
            return try {
                Gson().fromJson(rawJsonStr, VCheckDesignConfig::class.java)
            } catch (e: Exception) {
                Log.w(VCheckSDK.TAG, "VCheckSDK - warning: Non-valid JSON was passed while " +
                        "initializing VCheckDesignConfig instance; trying to set VCheck default theme...")
                Gson().fromJson(defaultThemeJsonStr, VCheckDesignConfig::class.java)
            }
        }

        private const val defaultThemeJsonStr: String =
            """
            {
               "primary": "#2E75FF",
               "primaryHover": "#2E96FF",
               "primaryActive": "#3361EC",
               "primaryContent": "#FFFFFF",
               "primaryBg": "#5D6884",
               "accent": "#6096FF",
               "accentHover": "#6ABFFF",
               "accentActive": "#4F79F7",
               "accentContent": "#FFFFFF",
               "accentBg": "#32404A",
               "neutral": "#FFFFFF",
               "neutralHover": "rgba(255, 255, 255, 0.4)",
               "neutralActive": "rgba(255, 255, 255, 0.1)",
               "neutralContent": "#000000",
        
               "success": "#6CFB93",
               "successHover": "#C8FDD2",
               "successActive": "#00DF53",
               "successBg": "#3A4B3F",
               "successContent": "#3B3B3B",
               "error": "#F47368",
               "errorHover": "#FF877C",
               "errorActive": "#DE473A",
               "errorBg": "#4B2A24",
               "errorContent": "#3B3B3B",
               "warning": "#FFB482",
               "warningHover": "#FFBF94",
               "warningActive": "#D3834E",
               "warningBg": "#3F3229",
               "warningContent": "#3B3B3B",
        
               "base": "#2A2A2A",
               "base_100": "#3C3C3C",
               "base_200": "#555555",
               "base_300": "#6A6A6A",
               "base_400": "#7F7F7F",
               "base_500": "#949494",
               "baseContent": "#FFFFFF",
               "baseSecondaryContent": "#D8D8D8",
               "disabled": "#AAAAAA",
               "disabledContent": "#6A6A6A"
            }
            """
    }
}

/**
{
"primary": "#2E75FF",  -- bg of primary action buttons, all primary accent elements
"primaryHover": "#2E96FF",  -- lighter primary color
"primaryActive": "#3361EC",  -- darker primary color (on pressed etc.) (?)
"primaryContent": "#FFFFFF",  -- text content, etc.
"primaryBg": "#5D6884",
"accent": "#6096FF",
"accentHover": "#6ABFFF",
"accentActive": "#4F79F7",
"accentContent": "#FFFFFF",
"accentBg": "#32404A",
"neutral": "#FFFFFF",  -- e.g. borders of non-primary text buttons
"neutralHover": "rgba(255, 255, 255, 0.4)",
"neutralActive": "rgba(255, 255, 255, 0.1)",
"neutralContent": "#000000",  -- e.g. text color inside non-primary text buttons (?)

"success": "#6CFB93",  -- success variations (liveness UI etc.)
"successHover": "#C8FDD2",  -- success variations (liveness UI etc.)
"successActive": "#00DF53",  -- success variations (liveness UI etc.)
"successBg": "#3A4B3F",  -- success variations (liveness UI etc.)
"successContent": "#3B3B3B",  -- success variations (liveness UI etc.)
"error": "#F47368",
"errorHover": "#FF877C",
"errorActive": "#DE473A",
"errorBg": "#4B2A24",
"errorContent": "#3B3B3B",
"warning": "#FFB482",
"warningHover": "#FFBF94",
"warningActive": "#D3834E",
"warningBg": "#3F3229",
"warningContent": "#3B3B3B",

"base": "#2A2A2A",  -- primary screen backgrounds
"base_100": "#3C3C3C",  -- secondary screen backgrounds
"base_200": "#555555", -- tertiary (card) backgrounds
"base_300": "#6A6A6A", -- button cards borders
"base_400": "#7F7F7F", -- sections borders (photo previews etc.) + text fields borders
"base_500": "#949494",
"baseContent": "#FFFFFF", -- texts and util icon buttons
"baseSecondaryContent": "#D8D8D8", -- secondary texts
"disabled": "#AAAAAA",  -- bg of disabled buttons
"disabledContent": "#6A6A6A"  -- text/content of disabled buttons
}
 */