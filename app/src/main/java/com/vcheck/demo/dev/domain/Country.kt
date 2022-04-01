package com.vcheck.demo.dev.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Country(val name: String, val flag: Int): Parcelable