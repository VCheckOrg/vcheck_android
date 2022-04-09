package com.vcheck.demo.dev.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocTypeTO(
    val docType: DocType
) : Parcelable

enum class DocType {
    INNER_PASSPORT_OR_COMMON,
    FOREIGN_PASSPORT,
    ID_CARD
}

fun DocType.toCategoryIdx(): Int {
    return when(this) {
        DocType.INNER_PASSPORT_OR_COMMON -> 0
        DocType.FOREIGN_PASSPORT -> 1
        DocType.ID_CARD -> 2
    }
}

fun docCategoryIdxToType(categoryIdx: Int): DocType {
    return when(categoryIdx) {
        0 -> DocType.INNER_PASSPORT_OR_COMMON
        1 -> DocType.FOREIGN_PASSPORT
        else -> DocType.ID_CARD
    }
}