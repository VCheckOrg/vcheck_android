package com.vcheck.sdk.core.presentation.doc_photo_auto_parsing.ui

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
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.VCheckSDK.TAG
import com.vcheck.sdk.core.databinding.FragmentSegmentationStartBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.DocType
import com.vcheck.sdk.core.domain.docCategoryIdxToType
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.doc_photo_auto_parsing.VCheckSegmentationActivity
import com.vcheck.sdk.core.presentation.transferrable_objects.PhotoUploadType
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

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
                                VCheckDIContainer.mainRepository.getCheckDocPhotosTO()!!, PhotoUploadType.AUTO)
                        findNavController().navigate(action)
                    } else {
                        Log.d(TAG, "Photo transferrable object was not set")
                    }
                } else {
                    findNavController().navigate(R.id.action_global_photoUploadScreen)
                }
            } else {
                //Log.d(TAG, "Back press from SegmentationActivity")
            }
        }
    }

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.launchSegmentationButton.setBackgroundColor(Color.parseColor(it))
            _binding!!.docImageFrontPart.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.noTimeBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.docTitle.setTextColor(Color.parseColor(it))
            _binding!!.backArrow.setColorFilter(Color.parseColor(it))
            _binding!!.docImageMidPart.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            _binding!!.docSubtitle.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.docImageBackPartTwo.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryBg?.let {
            _binding!!.docImageBackPartOne.setColorFilter(Color.parseColor(it))
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
            .getSelectedDocTypeWithData()?.category ?: 0)) {
            DocType.ID_CARD -> {
                //skip 1st z-layer image setting for id card doc type
                _binding!!.docImageBackPartOne.isVisible = false
                _binding!!.docImageBackPartTwo.setImageResource(R.drawable.il_doc_id_card_back_2)
                _binding!!.docImageMidPart.setImageResource(R.drawable.il_doc_id_card_mid)
                _binding!!.docImageFrontPart.setImageResource(R.drawable.il_doc_id_card_avatar)
                _binding!!.docTitle.setText(R.string.segmentation_instr_id_card_title)
                _binding!!.docSubtitle.setText(R.string.segmentation_instr_id_card_descr)
            }
            DocType.FOREIGN_PASSPORT -> {
                _binding!!.docImageBackPartOne.isVisible = true
                _binding!!.docImageBackPartOne.setImageResource(R.drawable.il_doc_int_back_1)
                _binding!!.docImageBackPartTwo.setImageResource(R.drawable.il_doc_int_back_2)
                _binding!!.docImageMidPart.setImageResource(R.drawable.il_doc_int_mid)
                _binding!!.docImageFrontPart.setImageResource(R.drawable.il_doc_int_avatar)
                _binding!!.docTitle.setText(R.string.segmentation_instr_foreign_passport_title)
                _binding!!.docSubtitle.setText(R.string.segmentation_instr_foreign_passport_descr)
            }
            else -> {
                _binding!!.docImageBackPartOne.isVisible = true
                _binding!!.docImageBackPartOne.setImageResource(R.drawable.il_doc_ukr_back_1)
                _binding!!.docImageBackPartTwo.setImageResource(R.drawable.il_doc_ukr_back_2)
                _binding!!.docImageMidPart.setImageResource(R.drawable.il_doc_ukr_mid)
                _binding!!.docImageFrontPart.setImageResource(R.drawable.il_doc_ukr_avatar)
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