package com.vcheck.sdk.core.presentation.transferrable_objects

import android.os.Parcelable
import com.vcheck.sdk.core.domain.DocType
import com.vcheck.sdk.core.domain.DocumentVerificationCode
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckPhotoDataTO(
    val selectedDocType: DocType,
    val photo1Path: String,
    val photo2Path: String?
) : Parcelable

@Parcelize
data class CheckDocInfoDataTO(
    val selectedDocType: DocType?,
    val docId: Int?,
    val photo1Path: String,
    val photo2Path: String?,
    val isForced: Boolean = false,
    val verificationErrorCode: DocumentVerificationCode? = null
    //val optCodeWithMessage: String = ""
) : Parcelable


@Parcelize
data class ZoomPhotoTO(
    val photo1Path: String?,
    val photo2Path: String?
) : Parcelable