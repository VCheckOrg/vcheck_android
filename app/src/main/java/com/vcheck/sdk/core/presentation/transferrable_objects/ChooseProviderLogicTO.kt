package com.vcheck.sdk.core.presentation.transferrable_objects

import android.os.Parcelable
import com.vcheck.sdk.core.domain.Provider
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChooseProviderLogicTO(
    val providers: List<Provider>
): Parcelable