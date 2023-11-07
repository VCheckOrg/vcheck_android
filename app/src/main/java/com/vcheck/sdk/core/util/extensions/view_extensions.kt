package com.vcheck.sdk.core.util.extensions

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

fun View.setMargins(
    leftMarginDp: Int? = null,
    topMarginDp: Int? = null,
    rightMarginDp: Int? = null,
    bottomMarginDp: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        leftMarginDp?.run { params.leftMargin = this.dpToPx(context) }
        topMarginDp?.run { params.topMargin = this.dpToPx(context) }
        rightMarginDp?.run { params.rightMargin = this.dpToPx(context) }
        bottomMarginDp?.run { params.bottomMargin = this.dpToPx(context) }
        requestLayout()
    }
}

//fun ImageView.setSVGColor(colorHex: String) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        this.colorFilter = BlendModeColorFilter(Color.parseColor(colorHex), BlendMode.SRC_ATOP)
//    } else {
//        this.drawable.setColorFilter(Color.parseColor(colorHex), PorterDuff.Mode.SRC_ATOP)
//    }
//}