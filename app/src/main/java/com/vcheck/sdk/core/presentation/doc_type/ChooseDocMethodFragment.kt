package com.vcheck.sdk.core.presentation.doc_type

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentChooseDocMethodBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.DocType
import com.vcheck.sdk.core.domain.DocTypeData
import com.vcheck.sdk.core.domain.docCategoryIdxToType
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import com.vcheck.sdk.core.util.extensions.checkUserInteractionCompletedForResult


class ChooseDocMethodFragment : ThemeWrapperFragment() {

    private var _binding: FragmentChooseDocMethodBinding? = null

    private lateinit var _viewModel: ChooseDocMethodViewModel

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.chooseDocMethodBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundTertiaryColorHex?.let {
            _binding!!.docMethodInnerPassportBackground.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.docMethodForeignPassportBackground.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.docMethodIdCardBackGround.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.chooseDocMethodTitle.setTextColor(Color.parseColor(it))
            _binding!!.docMethodTitle1.setTextColor(Color.parseColor(it))
            _binding!!.docMethodTitle2.setTextColor(Color.parseColor(it))
            _binding!!.docMethodTitle3.setTextColor(Color.parseColor(it))
            _binding!!.backArrow.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            _binding!!.chooseDocMethodDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
            _binding!!.docMethodInnerPassport.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.docMethodForeignPassport.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.docMethodIdCard.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.docMethodIcon1.setColorFilter(Color.parseColor(it))
            _binding!!.docMethodIcon2.setColorFilter(Color.parseColor(it))
            _binding!!.docMethodIcon3.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = ChooseDocMethodViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_doc_method, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChooseDocMethodBinding.bind(view)

        changeColorsToCustomIfPresent()

        _binding!!.backArrow.setOnClickListener {
            findNavController().popBackStack()
            findNavController().popBackStack()
        }

        _binding!!.docMethodInnerPassport.isVisible = false
        _binding!!.docMethodForeignPassport.isVisible = false
        _binding!!.docMethodIdCard.isVisible = false

        val selectedCountryCode = VCheckSDK.getOptSelectedCountryCode()!!
        //assuming that provider should have country in the scenario of country choosing

        _viewModel.docTypesResponse.observe(viewLifecycleOwner) {

            (requireActivity() as AppCompatActivity)
                .checkUserInteractionCompletedForResult(it.data?.errorCode)

            if (it.data?.data != null) {
                it.data.data.forEach { docTypeData ->
                    when (docCategoryIdxToType(docTypeData.category)) {
                        DocType.INNER_PASSPORT_OR_COMMON -> {
                            _binding!!.docMethodInnerPassport.isVisible = true
                            _binding!!.docMethodInnerPassport.setOnClickListener {
                                selectDocTypeDataAndNavigateForward(docTypeData)
                            }
                        }
                        DocType.FOREIGN_PASSPORT -> {
                            _binding!!.docMethodForeignPassport.isVisible = true
                            _binding!!.docMethodForeignPassport.setOnClickListener {
                                selectDocTypeDataAndNavigateForward(docTypeData)
                            }
                        }
                        DocType.ID_CARD -> {
                            _binding!!.docMethodIdCard.isVisible = true
                            _binding!!.docMethodIdCard.setOnClickListener {
                                selectDocTypeDataAndNavigateForward(docTypeData)
                            }
                        }
                    }
                }
            }
        }
        _viewModel.getAvailableDocTypes(selectedCountryCode)
    }

    private fun selectDocTypeDataAndNavigateForward(docTypeData: DocTypeData) {
        _viewModel.repository.setSelectedDocTypeWithData(docTypeData)
        findNavController().navigate(R.id.action_chooseDocMethodScreen_to_photoInstructionsFragment)
    }
}