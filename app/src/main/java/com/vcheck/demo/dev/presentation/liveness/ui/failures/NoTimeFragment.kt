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
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.NoTimeFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.VCheckLivenessActivity
import com.vcheck.demo.dev.util.ThemeWrapperFragment

class NoTimeFragment : ThemeWrapperFragment() {

    private var _binding: NoTimeFragmentBinding? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.tryAgainButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundPrimaryColorHex?.let {
            _binding!!.noTimeBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.textColorHex?.let {
            _binding!!.noTimeTitle.setTextColor(Color.parseColor(it))
            _binding!!.noTimeSubtitle.setTextColor(Color.parseColor(it))
            _binding!!.tryAgainButton.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.no_time_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = NoTimeFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        (activity as VCheckLivenessActivity).finishLivenessSession()

        _binding!!.tryAgainButton.setOnClickListener {
            findNavController().popBackStack()
            (activity as VCheckLivenessActivity).recreate()
        }
    }
}