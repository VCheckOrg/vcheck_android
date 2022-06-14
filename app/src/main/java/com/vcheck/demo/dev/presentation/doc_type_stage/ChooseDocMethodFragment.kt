package com.vcheck.demo.dev.presentation.doc_type_stage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.databinding.ChooseDocMethodFragmentBinding
import com.vcheck.demo.dev.domain.DocType
import com.vcheck.demo.dev.domain.DocTypeData
import com.vcheck.demo.dev.domain.docCategoryIdxToType
import com.vcheck.demo.dev.presentation.VCheckMainActivity

class ChooseDocMethodFragment : Fragment() {

    private var _binding: ChooseDocMethodFragmentBinding? = null

    private lateinit var _viewModel: ChooseDocMethodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VCheckSDKApp).appContainer
        _viewModel =
            ChooseDocMethodViewModel(appContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choose_doc_method_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ChooseDocMethodFragmentBinding.bind(view)

        _binding!!.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        _binding!!.docMethodInnerPassport.isVisible = false
        _binding!!.docMethodForeignPassport.isVisible = false
        _binding!!.docMethodIdCard.isVisible = false

        val selectedCountryCode =
            _viewModel.repository.getSelectedCountryCode(activity as VCheckMainActivity)

        _viewModel.setVerifToken(_viewModel.repository.getVerifToken((activity as VCheckMainActivity)))

        _viewModel.docTypesResponse.observe(viewLifecycleOwner) {
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
        Log.d("DOC_TYPE_DATA", docTypeData.toString())
        _viewModel.repository.setSelectedDocTypeWithData(docTypeData)
        findNavController().navigate(R.id.action_chooseDocMethodScreen_to_photoInstructionsFragment)
    }
}