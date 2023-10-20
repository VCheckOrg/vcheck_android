package com.vcheck.sdk.core.presentation.segmentation.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentSegErrorBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.sdk.core.presentation.transferrable_objects.PhotoUploadType
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class SegErrorFragment : ThemeWrapperFragment() {

    private val mStartForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (!it.data!!.getBooleanExtra("is_back_press", false)) {
                if (!it.data!!.getBooleanExtra("is_timeout_to_manual", false)) {
                    if (VCheckDIContainer.mainRepository.getCheckDocPhotosTO() != null) {
                        val action = SegErrorFragmentDirections
                            .actionSegErrorFragmentToCheckPhotoFragment(
                                VCheckDIContainer.mainRepository.getCheckDocPhotosTO()!!, PhotoUploadType.MANUAL)
                        findNavController().navigate(action)
                    } else {
                        Log.d(VCheckSDK.TAG, "Photo transferrable object was not set")
                    }
                } else {
                    findNavController().navigate(R.id.action_segErrorFragment_to_photoInstructionsFragment)
                }
            } else {
                Log.d(VCheckSDK.TAG, "Back press from SegmentationActivity")
            }
        }
    }

    private var _binding: FragmentSegErrorBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_seg_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSegErrorBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.replacePhotoButton.setOnClickListener {
            VCheckDIContainer.mainRepository.setManualPhotoUpload()
            findNavController().navigate(R.id.action_segErrorFragment_to_photoInstructionsFragment)
        }

        _binding!!.tryAgainButton.setOnClickListener {
            val intent = Intent((activity as VCheckMainActivity), VCheckSegmentationActivity::class.java)
            mStartForResult.launch(intent)
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
    }
}