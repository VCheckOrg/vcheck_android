package com.vcheck.demo.dev.presentation.screens

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.ErrorFragmentBinding
import com.vcheck.demo.dev.util.ThemeWrapperFragment

class ErrorFragment : ThemeWrapperFragment() {

    private var _binding: ErrorFragmentBinding? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.errorButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundPrimaryColorHex?.let {
            _binding!!.errorBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.textColorHex?.let {
            _binding!!.errorTitle.setTextColor(Color.parseColor(it))
            _binding!!.errorButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.descriptionTextColorHex?.let {
            _binding!!.errorDescription.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.error_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ErrorFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        _binding!!.errorButton.setOnClickListener {
            //here nav logic may be more complex
            findNavController().popBackStack()
        }
    }
}