package com.vcheck.sdk.core.util

import androidx.fragment.app.Fragment

abstract class ThemeWrapperFragment: Fragment() {

    abstract fun changeColorsToCustomIfPresent()
}