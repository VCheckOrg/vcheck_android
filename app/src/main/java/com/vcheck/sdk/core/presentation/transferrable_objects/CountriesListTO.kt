package com.vcheck.sdk.core.presentation.transferrable_objects

import android.os.Parcelable
import com.vcheck.sdk.core.domain.CountryTO
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountriesListTO(
    val countriesList: ArrayList<CountryTO>
) : Parcelable