package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.CheckPhotoFragmentBinding
import com.vcheck.demo.dev.domain.DocumentUploadRequestBody
import com.vcheck.demo.dev.domain.toCategoryIdx
import com.vcheck.demo.dev.presentation.MainActivity
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

            val docImage1File = BitmapFactory.decodeFile(args.checkPhotoDataTO.photo1Path)
            passportImage1.setImageBitmap(docImage1File)

            if (args.checkPhotoDataTO.photo2Path != null) {
                val docImage2File = BitmapFactory.decodeFile(args.checkPhotoDataTO.photo2Path)
                passportImage2.setImageBitmap(docImage2File)
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

            replacePhotoButton.setOnClickListener {
                //TODO
            }

            confirmPhotoButton.setOnClickListener {
                val body = DocumentUploadRequestBody(
                    _viewModel.repository.getSelectedCountryCode(activity as MainActivity),
                    args.checkPhotoDataTO.selectedDocType.toCategoryIdx(),
                    _isDocHandwritten
                )

                val multipartList: ArrayList<MultipartBody.Part> = ArrayList()
                val photoFile1 = File(args.checkPhotoDataTO.photo1Path)
                val filePartPhoto1: MultipartBody.Part = createFormData(
                    "jpeg", photoFile1.name, photoFile1.asRequestBody("image/*".toMediaType()))
                multipartList.add(filePartPhoto1)

                if (args.checkPhotoDataTO.photo2Path != null) {
                    val photoFile2 = File(args.checkPhotoDataTO.photo2Path!!)
                    val filePartPhoto2: MultipartBody.Part = createFormData(
                        "jpeg", photoFile2.name, photoFile1.asRequestBody("image/*".toMediaType()))
                    multipartList.add(filePartPhoto2)
                }

                _viewModel.uploadVerificationDocuments(
                    _viewModel.repository.getVerifToken(activity as MainActivity), body, multipartList)
            }

            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                if (it.data?.data != null) {
                    //TODO
                }
            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                if (it != null) Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
        }

    }
}