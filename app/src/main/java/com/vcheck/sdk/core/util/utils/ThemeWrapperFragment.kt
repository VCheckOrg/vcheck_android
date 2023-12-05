package com.vcheck.sdk.core.util.utils

import androidx.fragment.app.Fragment

abstract class ThemeWrapperFragment: Fragment() {

    abstract fun changeColorsToCustomIfPresent()
}