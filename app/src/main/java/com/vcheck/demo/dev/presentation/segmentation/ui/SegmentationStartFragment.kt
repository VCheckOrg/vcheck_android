package com.vcheck.demo.dev.presentation.segmentation.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.FragmentSegmentationStartBinding
import com.vcheck.demo.dev.di.VCheckDIContainer
import com.vcheck.demo.dev.domain.DocType
import com.vcheck.demo.dev.domain.docCategoryIdxToType
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.demo.dev.util.ThemeWrapperFragment

class SegmentationStartFragment : ThemeWrapperFragment() {

    private var _binding: FragmentSegmentationStartBinding? = null

    private val mStartForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (VCheckDIContainer.mainRepository.getCheckDocPhotosTO() != null) {
            val action = SegmentationStartFragmentDirections
                .actionSegmentationStartFragmentToCheckPhotoFragment(
                    VCheckDIContainer.mainRepository.getCheckDocPhotosTO()!!)
            findNavController().navigate(action)
        } else {
            Log.d("SEG", "Error: photo TO was not set!")
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