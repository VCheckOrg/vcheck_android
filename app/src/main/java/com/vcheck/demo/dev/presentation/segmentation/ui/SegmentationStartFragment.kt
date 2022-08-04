package com.vcheck.demo.dev.presentation.segmentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentSegmentationStartBinding
import com.vcheck.demo.dev.di.VCheckDIContainer
import com.vcheck.demo.dev.domain.DocType
import com.vcheck.demo.dev.domain.docCategoryIdxToType
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.presentation.segmentation.VCheckSegmentationActivity

class SegmentationStartFragment : Fragment() {

    private var _binding: FragmentSegmentationStartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_segmentation_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSegmentationStartBinding.bind(view)

        when (docCategoryIdxToType(VCheckDIContainer.mainRepository
            .getSelectedDocTypeWithData()!!.category)) {
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

            val mStartForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) {
                Log.d("SEG", "------------ SEGMENTATION ACTIVITY FINISHED!")
                if (VCheckDIContainer.mainRepository.getCheckDocPhotosTO() != null) {
                    val action = SegmentationStartFragmentDirections
                        .actionSegmentationStartFragmentToCheckPhotoFragment(
                            VCheckDIContainer.mainRepository.getCheckDocPhotosTO()!!)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText((activity as VCheckMainActivity),
                        "Error: photo TO was not set!", Toast.LENGTH_LONG).show()
                }
            }
            val intent = Intent((activity as VCheckMainActivity), VCheckSegmentationActivity::class.java)
            mStartForResult.launch(intent)
        }

    }
}