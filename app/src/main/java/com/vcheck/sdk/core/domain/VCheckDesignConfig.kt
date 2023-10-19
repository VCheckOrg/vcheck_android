package com.vcheck.sdk.core.domain

import com.google.gson.annotations.SerializedName

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
)
