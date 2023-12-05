package com.vcheck.sdk.core.presentation.liveness.ui.failures

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentErrorTooDarkBinding
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class TooDarkFragment : ThemeWrapperFragment() {

    private var _binding: FragmentErrorTooDarkBinding? = null

    private val args: TooDarkFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.tooDarkRepeatButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.tooDarkBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.tooDarkTitle.setTextColor(Color.parseColor(it))
            _binding!!.tooDarkDescription.setTextColor(Color.parseColor(it))
            //_binding!!.tooDarkRepeatButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.errorColorHex?.let {
            _binding!!.errorImage.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_too_dark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentErrorTooDarkBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.tooDarkRepeatButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as VCheckLivenessActivity).recreate()
        }
    }
}