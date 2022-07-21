package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.databinding.FragmentDocVerificationNotSuccessfulBinding
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.util.ThemeWrapperFragment

class DocVerifErrorFragment : ThemeWrapperFragment() {

    private val args: DocVerifErrorFragmentArgs by navArgs()

    private var _binding: FragmentDocVerificationNotSuccessfulBinding? = null

    private lateinit var viewModel: DocVerifErrorViewModel

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.errorTryAgainButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.docVerificationNotSuccessfulBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.errorTitle.setTextColor(Color.parseColor(it))
            _binding!!.errorTryAgainButton.setTextColor(Color.parseColor(it))
            _binding!!.pseudoBtnProceedAnyway.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            _binding!!.errorDescription.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (activity?.application as VCheckSDKApp).appContainer
        viewModel = DocVerifErrorViewModel(appContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doc_verification_not_successful, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDocVerificationNotSuccessfulBinding.bind(view)

        changeColorsToCustomIfPresent()

        //_binding!!.errorInfo.text = args.checkDocInfoDataTO.optCodeWithMessage

        _binding!!.errorTryAgainButton.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.action_global_chooseDocMethodScreen)
        }

        _binding!!.pseudoBtnProceedAnyway.setOnClickListener {
            viewModel.setDocumentAsPrimary(viewModel.repository.getVerifToken(activity as VCheckMainActivity),
                args.checkDocInfoDataTO.docId)
        }

        viewModel.primaryDocStatusResponse.observe(viewLifecycleOwner) {
            if (it) {
                val action = DocVerifErrorFragmentDirections
                    .actionDocVerificationNotSuccessfulFragmentToCheckDocInfoFragment(args.checkDocInfoDataTO,
                    args.checkDocInfoDataTO.docId)
                findNavController().navigate(action)
            }
        }
    }
}