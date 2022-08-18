package com.vcheck.sdk.core.presentation.photo_upload_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.PhotoInstructionsFragmentBinding
import com.vcheck.sdk.core.util.ThemeWrapperFragment

class PhotoInstructionsFragment : ThemeWrapperFragment() {

    private var _binding: PhotoInstructionsFragmentBinding? = null

    override fun changeColorsToCustomIfPresent() {

        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.photoInstructionsBackground.background = ColorDrawable(Color.parseColor(it))
            _binding!!.photoInstructionsScrollBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.photoInstructionsButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.photoInstructionsTitle.setTextColor(Color.parseColor(it))
            _binding!!.imageColorText.setTextColor(Color.parseColor(it))
            _binding!!.seenAllText.setTextColor(Color.parseColor(it))
            _binding!!.seenFourCornersText.setTextColor(Color.parseColor(it))
            _binding!!.validDispatchText.setTextColor(Color.parseColor(it))
            _binding!!.noForeignObjectsText.setTextColor(Color.parseColor(it))
            _binding!!.originalDocText.setTextColor(Color.parseColor(it))
            _binding!!.backArrow.setColorFilter(Color.parseColor(it))
            _binding!!.imageColorIcon.setColorFilter(Color.parseColor(it))
            _binding!!.seenAllIcon.setColorFilter(Color.parseColor(it))
            _binding!!.seenFourCornersIcon.setColorFilter(Color.parseColor(it))
            _binding!!.validDispatchIcon.setColorFilter(Color.parseColor(it))
            _binding!!.noForeignObjectsIcon.setColorFilter(Color.parseColor(it))
            _binding!!.originalDocIcon.setColorFilter(Color.parseColor(it))
            _binding!!.photoInstructionsButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            _binding!!.photoInstructionsDescription.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.photo_instructions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = PhotoInstructionsFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        _binding!!.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        _binding!!.photoInstructionsButton.setOnClickListener {
            val action = PhotoInstructionsFragmentDirections.actionPhotoInstructionsFragmentToPhotoUploadScreen()
            findNavController().navigate(action)
        }
    }
}