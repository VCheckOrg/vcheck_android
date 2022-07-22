package com.vcheck.demo.dev.presentation.transferrable_objects

import android.os.Parcelable
import com.vcheck.demo.dev.domain.DocType
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckPhotoDataTO(
    val selectedDocType: DocType,
    val photo1Path: String,
    val photo2Path: String?
) : Parcelable

@Parcelize
data class CheckDocInfoDataTO(
    val selectedDocType: DocType,
    val docId: Int,
    val photo1Path: String,
    val photo2Path: String?,
    val isForced: Boolean = false,
    val optCodeWithMessage: String = ""
) : Parcelable


@Parcelize
data class ZoomPhotoTO(
    val photo1Path: String?,
    val photo2Path: String?
) : Parcelable