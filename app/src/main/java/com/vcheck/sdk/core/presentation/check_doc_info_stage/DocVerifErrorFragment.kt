package com.vcheck.sdk.core.presentation.check_doc_info_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentDocVerificationNotSuccessfulBinding
import com.vcheck.sdk.core.domain.DocumentVerificationCode
import com.vcheck.sdk.core.util.ThemeWrapperFragment

class DocVerifErrorFragment : ThemeWrapperFragment() {

    private val args: DocVerifErrorFragmentArgs by navArgs()

    private var _binding: FragmentDocVerificationNotSuccessfulBinding? = null

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
            //_binding!!.errorTryAgainButton.setTextColor(Color.parseColor(it))
            _binding!!.pseudoBtnProceedAnyway.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            _binding!!.errorDescription.setTextColor(Color.parseColor(it))
        }
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

        _binding!!.errorDescription.text = getCodeStringResource(args.checkDocInfoDataTO.verificationErrorCode)

        _binding!!.errorTryAgainButton.setOnClickListener {
            findNavController().popBackStack()
            findNavController().navigate(R.id.action_global_chooseDocMethodScreen)
        }

        if (args.checkDocInfoDataTO.docId != null) {
            _binding!!.pseudoBtnProceedAnyway.setOnClickListener {
                val action = DocVerifErrorFragmentDirections
                    .actionDocVerificationNotSuccessfulFragmentToCheckDocInfoFragment(
                        args.checkDocInfoDataTO,
                        args.checkDocInfoDataTO.docId!!
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun getCodeStringResource(code: DocumentVerificationCode?): String {
        return when (code) {
            DocumentVerificationCode.VERIFICATION_NOT_INITIALIZED -> getString(R.string.doc_verification_verification_not_initialized)
            DocumentVerificationCode.USER_INTERACTED_COMPLETED -> getString(R.string.doc_verification_user_interacted_completed)
            DocumentVerificationCode.STAGE_NOT_FOUND -> getString(R.string.doc_verification_stage_not_found)
            DocumentVerificationCode.INVALID_STAGE_TYPE -> getString(R.string.doc_verification_invalid_stage_type)
            DocumentVerificationCode.PRIMARY_DOCUMENT_EXISTS -> getString(R.string.doc_verification_primary_document_exists)
            DocumentVerificationCode.UPLOAD_ATTEMPTS_EXCEEDED -> getString(R.string.doc_verification_upload_attempts_exceeded)
            DocumentVerificationCode.INVALID_DOCUMENT_TYPE -> getString(R.string.doc_verification_invalid_document_type)
            DocumentVerificationCode.INVALID_PAGES_COUNT -> getString(R.string.doc_verification_invalid_pages_count)
            DocumentVerificationCode.INVALID_FILES -> getString(R.string.doc_verification_invalid_files)
            DocumentVerificationCode.PHOTO_TOO_LARGE -> getString(R.string.doc_verification_photo_too_large)
            DocumentVerificationCode.PARSING_ERROR -> getString(R.string.doc_verification_parsing_error)
            DocumentVerificationCode.INVALID_PAGE -> getString(R.string.doc_verification_invalid_page)
            DocumentVerificationCode.FRAUD -> getString(R.string.doc_verification_fraud)
            DocumentVerificationCode.BLUR -> getString(R.string.doc_verification_blur)
            else -> "Unknown code"
        }
    }
}