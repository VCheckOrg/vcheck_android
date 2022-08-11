package com.vcheck.demo.dev.presentation.segmentation.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.FragmentSegTimeoutBinding
import com.vcheck.demo.dev.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.demo.dev.util.ThemeWrapperFragment

class SegTimeoutFragment : ThemeWrapperFragment() {

    private var _binding: FragmentSegTimeoutBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.no_time_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSegTimeoutBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        //(activity as VCheckSegmentationActivity).finishLivenessSession()

        _binding!!.tryAgainButton.setOnClickListener {
            findNavController().popBackStack()
            (activity as VCheckSegmentationActivity).recreate()
        }
    }

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.tryAgainButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.noTimeBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.noTimeTitle.setTextColor(Color.parseColor(it))
            _binding!!.tryAgainButton.setTextColor(Color.parseColor(it))
        }
    }
}
