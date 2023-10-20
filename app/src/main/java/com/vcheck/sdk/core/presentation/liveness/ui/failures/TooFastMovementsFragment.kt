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
import com.vcheck.sdk.core.databinding.TooFastMovementsFragmentBinding
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class TooFastMovementsFragment : ThemeWrapperFragment() {

    private var _binding: TooFastMovementsFragmentBinding? = null

    private val args: TooFastMovementsFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.tooFastMovementsRepeatButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.tooFastMovementsBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.tooFastMovementsTitle.setTextColor(Color.parseColor(it))
            _binding!!.tooFastMovementsDescription.setTextColor(Color.parseColor(it))
            //_binding!!.tooFastMovementsRepeatButton.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.too_fast_movements_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = TooFastMovementsFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.tooFastMovementsRepeatButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as VCheckLivenessActivity).recreate()
        }
    }
}
