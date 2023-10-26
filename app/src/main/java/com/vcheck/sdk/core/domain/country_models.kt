package com.vcheck.sdk.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryTO(val name: String,
                     val code: String,
                     val flag: String,
                     val isBlocked : Boolean): Parcelable