package com.vcheck.demo.dev.util

import androidx.fragment.app.Fragment

abstract class ThemeWrapperFragment: Fragment() {

    abstract fun changeColorsToCustomIfPresent()
}