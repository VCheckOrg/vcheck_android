package com.vcheck.sdk.core.presentation.common_screens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentErrorFailVerificationBinding
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class FailVerificationFragment : ThemeWrapperFragment() {

    private var _binding: FragmentErrorFailVerificationBinding? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.errorColorHex?.let {
            _binding!!.errorImage.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_fail_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentErrorFailVerificationBinding.bind(view)

        changeColorsToCustomIfPresent()
    }
}
