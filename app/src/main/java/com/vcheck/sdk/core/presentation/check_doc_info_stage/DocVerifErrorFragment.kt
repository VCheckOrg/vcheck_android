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
import com.vcheck.sdk.core.databinding.FragmentErrorDocVerificationBinding
import com.vcheck.sdk.core.domain.DocumentVerificationCode
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class DocVerifErrorFragment : ThemeWrapperFragment() {

    private val args: DocVerifErrorFragmentArgs by navArgs()

    private var _binding: FragmentErrorDocVerificationBinding? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.errorTryAgainButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.docVerificationNotSuccessfulBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.errorTitle.setTextColor(Color.parseColor(it))
            //_binding!!.errorTryAgainButton.setTextColor(Color.parseColor(it))
            _binding!!.pseudoBtnProceedAnyway.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            _binding!!.errorDescription.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_doc_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentErrorDocVerificationBinding.bind(view)

        changeColorsToCustomIfPresent()

        _binding!!.errorTitle.text = getCodeStringTitleResource(
            args.checkDocInfoDataTO.verificationErrorCode)
        _binding!!.errorDescription.text = getCodeStringDescriptionResource(
            args.checkDocInfoDataTO.verificationErrorCode)

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

    private fun getCodeStringTitleResource(code: DocumentVerificationCode?): String {
        return when (code) {
            DocumentVerificationCode.VERIFICATION_NOT_INITIALIZED -> getString(R.string.doc_verif_default_title)
            DocumentVerificationCode.USER_INTERACTED_COMPLETED -> getString(R.string.doc_verif_default_title)
            DocumentVerificationCode.STAGE_NOT_FOUND -> getString(R.string.doc_verif_default_title)
            DocumentVerificationCode.INVALID_STAGE_TYPE -> getString(R.string.doc_verif_default_title)
            DocumentVerificationCode.PRIMARY_DOCUMENT_EXISTS -> getString(R.string.doc_verif_primary_already_exists_title)
            DocumentVerificationCode.UPLOAD_ATTEMPTS_EXCEEDED -> getString(R.string.doc_verif_upload_attempts_exceeded_title)
            DocumentVerificationCode.INVALID_DOCUMENT_TYPE -> getString(R.string.doc_verif_invalid_document_type_title)
            DocumentVerificationCode.INVALID_PAGES_COUNT -> getString(R.string.doc_verif_invalid_pages_count_title)
            DocumentVerificationCode.INVALID_FILES -> getString(R.string.doc_verif_invalid_files_title)
            DocumentVerificationCode.PHOTO_TOO_LARGE -> getString(R.string.doc_verif_invalid_files_title)
            DocumentVerificationCode.PARSING_ERROR -> getString(R.string.doc_verif_not_scanned_title)
            DocumentVerificationCode.INVALID_PAGE -> getString(R.string.doc_verif_invalid_page_title)
            DocumentVerificationCode.FRAUD -> getString(R.string.doc_verif_fraud_title)
            DocumentVerificationCode.BLUR -> getString(R.string.doc_verif_blur_title)
            null -> "Document Verification Error"
        }
    }

    private fun getCodeStringDescriptionResource(code: DocumentVerificationCode?): String {
        return when (code) {
            DocumentVerificationCode.VERIFICATION_NOT_INITIALIZED -> getString(R.string.doc_verif_default_text)
            DocumentVerificationCode.USER_INTERACTED_COMPLETED -> getString(R.string.doc_verif_default_text)
            DocumentVerificationCode.STAGE_NOT_FOUND -> getString(R.string.doc_verif_default_text)
            DocumentVerificationCode.INVALID_STAGE_TYPE -> getString(R.string.doc_verif_default_text)
            DocumentVerificationCode.PRIMARY_DOCUMENT_EXISTS -> getString(R.string.doc_verif_primary_already_exists_text)
            DocumentVerificationCode.UPLOAD_ATTEMPTS_EXCEEDED -> getString(R.string.doc_verif_upload_attempts_exceeded_text)
            DocumentVerificationCode.INVALID_DOCUMENT_TYPE -> getString(R.string.doc_verif_invalid_document_type_text)
            DocumentVerificationCode.INVALID_PAGES_COUNT -> getString(R.string.doc_verif_invalid_pages_count_text)
            DocumentVerificationCode.INVALID_FILES -> getString(R.string.doc_verif_invalid_files_text)
            DocumentVerificationCode.PHOTO_TOO_LARGE -> getString(R.string.doc_verif_invalid_files_text)
            DocumentVerificationCode.PARSING_ERROR -> getString(R.string.doc_verif_not_scanned_text)
            DocumentVerificationCode.INVALID_PAGE -> getString(R.string.doc_verif_invalid_page_text)
            DocumentVerificationCode.FRAUD -> getString(R.string.doc_verif_fraud_text)
            DocumentVerificationCode.BLUR -> getString(R.string.doc_verif_blur_text)
            null -> "Unknown code"
        }
    }
}