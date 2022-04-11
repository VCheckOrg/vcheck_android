package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.CheckPhotoFragmentBinding
import com.vcheck.demo.dev.domain.DocumentUploadRequestBody
import com.vcheck.demo.dev.domain.toCategoryIdx
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

    private var _isDocHandwritten: Boolean = false

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

            machinePrintCard.setOnClickListener {
                radioBtnMachineFilledDoc.isChecked = true
            }
            handPrintCard.setOnClickListener {
                radioBtnHandwrittenDoc.isChecked = true
            }

            radioBtnHandwrittenDoc.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    _isDocHandwritten = true
                    radioBtnMachineFilledDoc.isChecked = false
                }
            }
            radioBtnMachineFilledDoc.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    _isDocHandwritten = false
                    radioBtnHandwrittenDoc.isChecked = false
                }
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
                findNavController().navigate(R.id.action_global_photoUploadScreen)
            }

            confirmPhotoButton.setOnClickListener {
                val body = DocumentUploadRequestBody(
                    _viewModel.repository.getSelectedCountryCode(activity as MainActivity),
                    args.checkPhotoDataTO.selectedDocType.toCategoryIdx(),
                    _isDocHandwritten)

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

//                Log.i("PHOTOS", "----------------- MULTIPART LIST: ${multipartList.map { it.body.contentLength() }} | " +
//                        "${multipartList.map { it.body.contentType() }}")
//                Log.i("PHOTOS", "----------------- MULTIPART LIST: ${multipartList.size}")

                handPrintCard.isVisible = false
                machinePrintCard.isVisible = false
                replacePhotoButton.isVisible = false
                uploadDocPhotosLoadingIndicator.isVisible = true
                confirmPhotoButton.isVisible = false
                checkPhotoTitle2.setText(R.string.photo_upload_wait_disclaimer)

                _viewModel.uploadVerificationDocuments(
                    _viewModel.repository.getVerifToken(activity as MainActivity), body, multipartList)
            }

            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                if (it.data?.data != null) {
                    val action = CheckPhotoFragmentDirections
                        .actionCheckPhotoFragmentToCheckInfoFragment(
                            CheckDocInfoDataTO(args.checkPhotoDataTO.selectedDocType,
                                it.data.data.document,
                                args.checkPhotoDataTO.photo1Path,
                                args.checkPhotoDataTO.photo2Path)
                        )
                    findNavController().navigate(action)
                }
            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                handPrintCard.isVisible = true
                machinePrintCard.isVisible = true
                replacePhotoButton.isVisible = true
                confirmPhotoButton.isVisible = true
                uploadDocPhotosLoadingIndicator.isVisible = false
                checkPhotoTitle2.setText(R.string.check_photo_title_2)
                if (it != null) Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}