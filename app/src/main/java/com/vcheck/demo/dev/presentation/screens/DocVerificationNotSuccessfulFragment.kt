package com.vcheck.demo.dev.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentDocVerificationNotSuccessfulBinding

class DocVerificationNotSuccessfulFragment : Fragment(R.layout.fragment_doc_verification_not_successful) {

    private val args: DocVerificationNotSuccessfulFragmentArgs by navArgs()

    private var _binding: FragmentDocVerificationNotSuccessfulBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDocVerificationNotSuccessfulBinding.bind(view)

        _binding!!.errorButton.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.action_global_chooseDocMethodScreen)
        }

        _binding!!.pseudoBtnProceedAnyway.setOnClickListener {
            val action = DocVerificationNotSuccessfulFragmentDirections
                .actionDocVerificationNotSuccessfulFragmentToCheckDocInfoFragment(
                    args.checkDocInfoDataTO)
            findNavController().navigate(action)
        }
    }
}