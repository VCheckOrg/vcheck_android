package com.vcheck.demo.dev.presentation.transferrable_objects

import android.os.Parcelable
import com.vcheck.demo.dev.domain.CountryTO
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountriesListTO(
    val countriesList: ArrayList<CountryTO>
) : Parcelable