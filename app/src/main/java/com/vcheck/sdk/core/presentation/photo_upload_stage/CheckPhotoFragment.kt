package com.vcheck.sdk.core.presentation.photo_upload_stage

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.CheckPhotoFragmentBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.transferrable_objects.CheckDocInfoDataTO
import com.vcheck.sdk.core.presentation.transferrable_objects.ZoomPhotoTO
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.Exception


class CheckPhotoFragment : ThemeWrapperFragment() {

    private lateinit var _viewModel: CheckPhotoViewModel
    private var _binding: CheckPhotoFragmentBinding? = null
    private val args: CheckPhotoFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.confirmPhotoButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.checkPhotoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundTertiaryColorHex?.let {
            _binding!!.photoCard1Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.photoCard2Background.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.checkPhotoTitle.setTextColor(Color.parseColor(it))
            _binding!!.tvProcessingDisclaimer.setTextColor(Color.parseColor(it))
            _binding!!.uploadDocPhotosLoadingIndicator.setIndicatorColor(Color.parseColor(it))
            //_binding!!.confirmPhotoButton.setTextColor(Color.parseColor(it))
            _binding!!.replacePhotoButton.setTextColor(Color.parseColor(it))
            _binding!!.replacePhotoButton.strokeColor = ColorStateList.valueOf(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            _binding!!.checkPhotoDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            _binding!!.photoCard1.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.photoCard2.setCardBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = CheckPhotoViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.check_photo_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = CheckPhotoFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.apply {

            tvProcessingDisclaimer.isVisible = false

            uploadDocPhotosLoadingIndicator.isVisible = false

            photoCard2.isVisible = false

            try {
                val docPhoto1File = File(args.checkPhotoDataTO.photo1Path)
                Picasso.get().load(docPhoto1File).fit().centerInside().into(passportImage1)

                if (args.checkPhotoDataTO.photo2Path != null) {
                    photoCard2.isVisible = true
                    val docPhoto2File = File(args.checkPhotoDataTO.photo2Path!!)
                    Picasso.get().load(docPhoto2File).fit().centerInside().into(passportImage2)
                } else {
                    photoCard2.isVisible = false
                }
            } catch (e: Error) {
                Toast.makeText(requireActivity(), e.localizedMessage, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), e.localizedMessage, Toast.LENGTH_LONG).show()
            }

            zoomIcon1.setOnClickListener {
                val action =
                    CheckPhotoFragmentDirections.actionCheckPhotoFragmentToZoomedPhotoScreen(
                        ZoomPhotoTO(args.checkPhotoDataTO.photo1Path, null))
                findNavController().navigate(action)
            }
            zoomIcon2.setOnClickListener {
                val action =
                    CheckPhotoFragmentDirections.actionCheckPhotoFragmentToZoomedPhotoScreen(
                        ZoomPhotoTO(null, args.checkPhotoDataTO.photo2Path))
                findNavController().navigate(action)
            }

            replacePhotoButton.setOnClickListener {
                findNavController().navigate(R.id.action_global_photoUploadScreen)
            }

            confirmPhotoButton.setOnClickListener {
                val body = DocumentUploadRequestBody(
                    VCheckSDK.getSelectedCountryCode(),
                    args.checkPhotoDataTO.selectedDocType.toCategoryIdx(),
                    _viewModel.repository.isPhotoUploadManual())

                val multipartList: ArrayList<MultipartBody.Part> = ArrayList()
                val photoFile1 = File(args.checkPhotoDataTO.photo1Path)
                val filePartPhoto1: MultipartBody.Part = createFormData(
                    "0.jpg", photoFile1.name, photoFile1.asRequestBody("image/jpeg".toMediaType())) // image/*
                multipartList.add(filePartPhoto1)

                if (args.checkPhotoDataTO.photo2Path != null) {
                    val photoFile2 = File(args.checkPhotoDataTO.photo2Path!!)
                    val filePartPhoto2: MultipartBody.Part = createFormData(
                        "1.jpg", photoFile2.name, photoFile2.asRequestBody("image/jpeg".toMediaType())) // image/*
                    multipartList.add(filePartPhoto2)
                }

                replacePhotoButton.isVisible = false
                confirmPhotoButton.isVisible = false
                uploadDocPhotosLoadingIndicator.isVisible = true
                tvProcessingDisclaimer.isVisible = true

                _viewModel.uploadVerificationDocuments(body, multipartList)
            }

            _viewModel.uploadErrorResponse.observe(viewLifecycleOwner) {
                handleDocErrorResponse(it)
            }

            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                handleDocUploadResponse(it)
            }
        }
    }

    private fun handleDocErrorResponse(response: BaseClientResponseModel?) {
        if (response != null) {
            if (response.errorCode == DocumentVerificationCode.PARSING_ERROR.toCodeIdx() ||
                response.errorCode == DocumentVerificationCode.INVALID_PAGE.toCodeIdx()) {
                if (response.data?.id != null) {
                    val action = CheckPhotoFragmentDirections
                        .actionCheckPhotoFragmentToDocVerificationNotSuccessfulFragment(
                            CheckDocInfoDataTO(args.checkPhotoDataTO.selectedDocType,
                                response.data.id,
                                args.checkPhotoDataTO.photo1Path,
                                args.checkPhotoDataTO.photo2Path,
                                true))
                    findNavController().navigate(action)
                }
            } else {
                _binding!!.replacePhotoButton.isVisible = true
                _binding!!.confirmPhotoButton.isVisible = true
                _binding!!.uploadDocPhotosLoadingIndicator.isVisible = false
                _binding!!.tvProcessingDisclaimer.isVisible = false
                Toast.makeText(activity, "Error: [${codeIdxToVerificationCode(response.errorCode)}]", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleDocUploadResponse(resource: Resource<DocumentUploadResponse>) {
        if (resource.data?.data != null) {
            if (resource.data.data.id != null) {
                val action = CheckPhotoFragmentDirections
                    .actionCheckPhotoFragmentToCheckInfoFragment(
                        CheckDocInfoDataTO(
                            args.checkPhotoDataTO.selectedDocType,
                            resource.data.data.id,
                            args.checkPhotoDataTO.photo1Path,
                            args.checkPhotoDataTO.photo2Path,
                            false),
                        resource.data.data.id)
                findNavController().navigate(action)
            } else {
                Toast.makeText(activity, getString(R.string.doc_verification_error_description), Toast.LENGTH_LONG).show()
            }
        }
    }

}