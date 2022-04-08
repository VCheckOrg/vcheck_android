package com.vcheck.demo.dev.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountriesListTO(
    val countriesList: ArrayList<CountryTO>
) : Parcelable