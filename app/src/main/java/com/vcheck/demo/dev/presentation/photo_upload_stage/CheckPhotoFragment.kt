package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.databinding.CheckPhotoFragmentBinding
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.transferrable_objects.CheckDocInfoDataTO
import com.vcheck.demo.dev.presentation.transferrable_objects.ZoomPhotoTO
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class CheckPhotoFragment : Fragment() {

    private lateinit var _viewModel: CheckPhotoViewModel

    private var _binding: CheckPhotoFragmentBinding? = null

    private val args: CheckPhotoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = CheckPhotoViewModel(appContainer.mainRepository)
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

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.apply {

            uploadDocPhotosLoadingIndicator.isVisible = false

            photoCard2.isVisible = false

            val docImage1File = BitmapFactory.decodeFile(args.checkPhotoDataTO.photo1Path)
            passportImage1.setImageBitmap(docImage1File)

            if (args.checkPhotoDataTO.photo2Path != null) {
                photoCard2.isVisible = true
                val docImage2File = BitmapFactory.decodeFile(args.checkPhotoDataTO.photo2Path)
                passportImage2.setImageBitmap(docImage2File)
            } else {
                photoCard2.isVisible = false
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
                findNavController().popBackStack()
                findNavController().navigate(R.id.action_global_chooseDocMethodScreen)
            }

            confirmPhotoButton.setOnClickListener {
                val body = DocumentUploadRequestBody(
                    _viewModel.repository.getSelectedCountryCode(activity as MainActivity),
                    args.checkPhotoDataTO.selectedDocType.toCategoryIdx())

                val multipartList: ArrayList<MultipartBody.Part> = ArrayList()
                val photoFile1 = File(args.checkPhotoDataTO.photo1Path)
                val filePartPhoto1: MultipartBody.Part = createFormData(
                    "1.jpg", photoFile1.name, photoFile1.asRequestBody("image/jpeg".toMediaType())) // image/*
                multipartList.add(filePartPhoto1)

                if (args.checkPhotoDataTO.photo2Path != null) {
                    val photoFile2 = File(args.checkPhotoDataTO.photo2Path!!)
                    val filePartPhoto2: MultipartBody.Part = createFormData(
                        "2.jpg", photoFile2.name, photoFile2.asRequestBody("image/jpeg".toMediaType())) // image/*
                    multipartList.add(filePartPhoto2)
                }

                replacePhotoButton.isVisible = false
                confirmPhotoButton.isVisible = false
                uploadDocPhotosLoadingIndicator.isVisible = true
                tvProcessingDisclaimer.isVisible = true

                _viewModel.uploadVerificationDocuments(
                    _viewModel.repository.getVerifToken(activity as MainActivity), body, multipartList)
            }

            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                handleDocUploadResponse(it)
            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                replacePhotoButton.isVisible = true
                confirmPhotoButton.isVisible = true
                uploadDocPhotosLoadingIndicator.isVisible = false
                tvProcessingDisclaimer.isVisible = false
                if (it != null) Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleDocUploadResponse(resource: Resource<DocumentUploadResponse>) {
        if (resource.data?.data != null) {
            if (resource.data.errorCode != 0 || resource.data.data.status != 0) {
                if (codeIdxToVerificationCode(resource.data.data.status) == DocumentVerificationCode.UploadAttemptsExceeded) {
                    val action = CheckPhotoFragmentDirections
                        .actionCheckPhotoFragmentToCheckInfoFragment(
                            CheckDocInfoDataTO(args.checkPhotoDataTO.selectedDocType,
                                resource.data.data.document,
                                args.checkPhotoDataTO.photo1Path,
                                args.checkPhotoDataTO.photo2Path))
                    findNavController().navigate(action)
                } else {
                    val errorInfo = "Service: [${resource.data.errorCode}] - " +
                            "${codeIdxToVerificationCode(resource.data.errorCode)}"
                    val statusInfo: String = "Parser: [${resource.data.data.status}] - " +
                            "${statusCodeToParsingStatus(resource.data.data.status)}"

                    val action = CheckPhotoFragmentDirections
                        .actionCheckPhotoFragmentToDocVerificationNotSuccessfulFragment(
                            CheckDocInfoDataTO(args.checkPhotoDataTO.selectedDocType,
                                resource.data.data.document,
                                args.checkPhotoDataTO.photo1Path,
                                args.checkPhotoDataTO.photo2Path,
                                errorInfo + "\n" + statusInfo
                            ))
                    findNavController().navigate(action)
                }
            } else {
                val action = CheckPhotoFragmentDirections
                    .actionCheckPhotoFragmentToCheckInfoFragment(
                        CheckDocInfoDataTO(args.checkPhotoDataTO.selectedDocType,
                            resource.data.data.document,
                            args.checkPhotoDataTO.photo1Path,
                            args.checkPhotoDataTO.photo2Path))
                findNavController().navigate(action)
            }
        }
    }
}