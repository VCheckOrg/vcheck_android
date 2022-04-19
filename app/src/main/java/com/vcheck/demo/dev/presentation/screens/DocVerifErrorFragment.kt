package com.vcheck.demo.dev.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.FragmentDocVerificationNotSuccessfulBinding
import com.vcheck.demo.dev.presentation.MainActivity

class DocVerifErrorFragment : Fragment(R.layout.fragment_doc_verification_not_successful) {

    private val args: DocVerifErrorFragmentArgs by navArgs()

    private var _binding: FragmentDocVerificationNotSuccessfulBinding? = null

    private lateinit var viewModel: DocVerifErrorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        viewModel = DocVerifErrorViewModel(appContainer.mainRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDocVerificationNotSuccessfulBinding.bind(view)

        _binding!!.errorInfo.text = args.checkDocInfoDataTO.optCodeWithMessage

        _binding!!.errorButton.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.action_global_chooseDocMethodScreen)
        }

        _binding!!.pseudoBtnProceedAnyway.setOnClickListener {
            viewModel.setDocumentAsPrimary(viewModel.repository.getVerifToken(activity as MainActivity),
                args.checkDocInfoDataTO.docId)
        }

        viewModel.primaryDocStatusResponse.observe(viewLifecycleOwner) {
            if (it) {
                val action = DocVerifErrorFragmentDirections
                    .actionDocVerificationNotSuccessfulFragmentToCheckDocInfoFragment(
                        args.checkDocInfoDataTO)
                findNavController().navigate(action)
            }
        }
    }
}