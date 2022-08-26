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
import com.vcheck.sdk.core.VCheckSDK.TAG
import com.vcheck.sdk.core.databinding.FragmentSegmentationStartBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.DocType
import com.vcheck.sdk.core.domain.docCategoryIdxToType
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.sdk.core.util.ThemeWrapperFragment

class SegmentationStartFragment : ThemeWrapperFragment() {

    private var _binding: FragmentSegmentationStartBinding? = null

    private val mStartForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (!it.data!!.getBooleanExtra("is_back_press", false)) {
                if (!it.data!!.getBooleanExtra("is_timeout_to_manual", false)) {
                    if (VCheckDIContainer.mainRepository.getCheckDocPhotosTO() != null) {
                        val action = SegmentationStartFragmentDirections
                            .actionSegmentationStartFragmentToCheckPhotoFragment(
                                VCheckDIContainer.mainRepository.getCheckDocPhotosTO()!!)
                        findNavController().navigate(action)
                    } else {
                        Log.d(TAG, "Photo transferrable object was not set")
                    }
                } else {
                    findNavController().navigate(R.id.action_segmentationStartFragment_to_photoInstructionsFragment)
                }
            } else {
                Log.d(TAG, "Back press from SegmentationActivity")
            }
        }
    }

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.launchSegmentationButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.noTimeBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.docTitle.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            _binding!!.docSubtitle.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_segmentation_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSegmentationStartBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        when (docCategoryIdxToType(VCheckDIContainer.mainRepository
            .getSelectedDocTypeWithData()?.category ?: 0)) { //!
            DocType.ID_CARD -> {
                _binding!!.docImage.setImageResource(R.drawable.img_id_card_large)
                _binding!!.docTitle.setText(R.string.segmentation_instr_id_card_title)
                _binding!!.docSubtitle.setText(R.string.segmentation_instr_id_card_descr)
            }
            DocType.FOREIGN_PASSPORT -> {
                _binding!!.docImage.setImageResource(R.drawable.img_internl_passport_large)
                _binding!!.docTitle.setText(R.string.segmentation_instr_foreign_passport_title)
                _binding!!.docSubtitle.setText(R.string.segmentation_instr_foreign_passport_descr)
            }
            else -> {
                _binding!!.docImage.setImageResource(R.drawable.img_ua_inner_passport_large)
                _binding!!.docTitle.setText(R.string.segmentation_instr_inner_passport_title)
                _binding!!.docSubtitle.setText(R.string.segmentation_instr_inner_passport_descr)
            }
        }

        _binding!!.launchSegmentationButton.setOnClickListener {
            val intent = Intent((activity as VCheckMainActivity), VCheckSegmentationActivity::class.java)
            mStartForResult.launch(intent)
        }

    }
}