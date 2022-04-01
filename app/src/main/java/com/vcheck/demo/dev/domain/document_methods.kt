package com.vcheck.demo.dev.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocMethodTO(
    val docMethod: DocMethod
): Parcelable

enum class DocMethod {
    INNER_PASSPORT,
    FOREIGN_PASSPORT,
    ID_CARD
}