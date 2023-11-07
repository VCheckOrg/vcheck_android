package com.vcheck.sdk.core.presentation.common_screens

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentErrorCommonBinding
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment


class ErrorFragment : ThemeWrapperFragment() {

    private var _binding: FragmentErrorCommonBinding? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.errorButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.errorBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.errorTitle.setTextColor(Color.parseColor(it))
            //_binding!!.errorButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            _binding!!.errorDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.errorColorHex?.let {
            _binding!!.errorImage.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_common, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentErrorCommonBinding.bind(view)

        changeColorsToCustomIfPresent()

        _binding!!.errorButton.setOnClickListener {
            //here nav logic may be more complex
            findNavController().popBackStack()
        }
    }
}