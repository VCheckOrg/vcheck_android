package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.FrameInterferenceFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.VCheckLivenessActivity
import com.vcheck.demo.dev.util.ThemeWrapperFragment

class FrameInterferenceFragment : ThemeWrapperFragment() {

    private var _binding: FrameInterferenceFragmentBinding? = null

    private val args: FrameInterferenceFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.frameInterferenceButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundPrimaryColorHex?.let {
            _binding!!.frameInterferenceBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.textColorHex?.let {
            _binding!!.frameInterferenceTitle.setTextColor(Color.parseColor(it))
            _binding!!.frameInterferenceDescription.setTextColor(Color.parseColor(it))
            _binding!!.frameInterferenceButton.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frame_interference_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FrameInterferenceFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.frameInterferenceButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as VCheckLivenessActivity).recreate()
        }
    }
}