package com.vcheck.demo.dev.presentation.transferrable_objects

import android.os.Parcelable
import com.vcheck.demo.dev.domain.DocType
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckPhotoDataTO(
    val selectedDocType: DocType,
    val photo1Path: String,
    val photo2Path: String?
): Parcelable