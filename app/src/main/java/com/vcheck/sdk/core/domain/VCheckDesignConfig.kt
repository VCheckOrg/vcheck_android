package com.vcheck.sdk.core.domain

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.VCheckSDKConstantsProvider

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
    var successColorHex: String? = null,
    @SerializedName("successHover")
    var successHover: String? = null,
    @SerializedName("successActive")
    var successActive: String? = null,
    @SerializedName("successBg")
    var successBg: String? = null,
    @SerializedName("successContent")
    var successContent: String? = null,
    @SerializedName("error")
    var errorColorHex: String? = null,
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
    var backgroundPrimaryColorHex: String? = null,
    @SerializedName("base_100")
    var backgroundSecondaryColorHex: String? = null,
    @SerializedName("base_200")
    var backgroundTertiaryColorHex: String? = null,
    @SerializedName("base_300")
    var buttonBorderColorHex: String? = null,
    //TODO: review where section color hex should be replaced by button color hex!
    @SerializedName("base_400")
    var sectionBorderColorHex: String? = null,
    @SerializedName("base_500")
    var base500: String? = null,
    @SerializedName("baseContent")
    var primaryTextColorHex: String? = null,
    @SerializedName("baseSecondaryContent")
    var secondaryTextColorHex: String? = null,
    @SerializedName("disabled")
    var disabled: String? = null,
    @SerializedName("disabledContent")
    var disabledContent: String? = null
) {

    companion object {

        @JvmStatic
        fun fromJSONStr(rawJsonStr: String) : VCheckDesignConfig {
            return try {
                Gson().fromJson(rawJsonStr, VCheckDesignConfig::class.java)
            } catch (e: JsonSyntaxException) {
                Log.w(VCheckSDK.TAG, "VCheckSDK - warning: Non-valid JSON was passed while " +
                        "initializing VCheckDesignConfig instance | trying to set VCheck default theme...")
                getDefaultThemeConfig()
            }
        }

        @JvmStatic
        fun getDefaultThemeConfig(): VCheckDesignConfig {
            return Gson().fromJson(VCheckSDKConstantsProvider.vcheckDefaultThemeConfig,
                VCheckDesignConfig::class.java)
        }
    }
}

/**
{
"primary": "#2E75FF",  -- bg of primary action buttons, all primary accent elements
"primaryHover": "#2E96FF",  -- lighter primary color
"primaryActive": "#3361EC",  -- darker primary color (on pressed etc.) (?)
"primaryContent": "#FFFFFF",  -- text content, etc. - content on primary buttons
"primaryBg": "#5D6884", - on text form press
"accent": "#6096FF", - n/u
"accentHover": "#6ABFFF", - n/u
"accentActive": "#4F79F7", - n/u
"accentContent": "#FFFFFF", - n/u
"accentBg": "#32404A", - n/u
"neutral": "#FFFFFF",  -- e.g. borders of non-primary text buttons
"neutralHover": "rgba(255, 255, 255, 0.4)", - for SDK, will use primary active color instead
"neutralActive": "rgba(255, 255, 255, 0.1)", - for SDK, will use primary active color instead
"neutralContent": "#000000",  -- e.g. text color inside non-primary text buttons

"success": "#6CFB93",  -- success variations (liveness UI etc.)
"successHover": "#C8FDD2", - n/u
"successActive": "#00DF53", - n/u
"successBg": "#3A4B3F", - n/u
"successContent": "#3B3B3B", - n/u
"error": "#F47368",  -- error common variations
"errorHover": "#FF877C", - n/u
"errorActive": "#DE473A", - n/u
"errorBg": "#4B2A24", - n/u
"errorContent": "#3B3B3B", - n/u
"warning": "#FFB482", - n/u
"warningHover": "#FFBF94", - n/u
"warningActive": "#D3834E", - n/u
"warningBg": "#3F3229", - n/u
"warningContent": "#3B3B3B", - n/u

"base": "#2A2A2A",  -- primary screen backgrounds
"base_100": "#3C3C3C",  -- secondary screen backgrounds
"base_200": "#555555", -- tertiary (card) backgrounds
"base_300": "#6A6A6A", -- button cards borders
"base_400": "#7F7F7F", -- sections borders (photo previews etc.) + text fields borders
"base_500": "#949494", - n/u (for now)
"baseContent": "#FFFFFF", -- texts and util icon buttons
"baseSecondaryContent": "#D8D8D8", -- secondary texts
"disabled": "#AAAAAA",  -- bg of disabled buttons
"disabledContent": "#6A6A6A"  -- text/content of disabled buttons
}
 */