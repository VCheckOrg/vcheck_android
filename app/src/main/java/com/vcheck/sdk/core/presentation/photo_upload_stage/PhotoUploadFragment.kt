package com.vcheck.sdk.core.presentation.photo_upload_stage

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.PhotoUploadFragmentBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.DocType
import com.vcheck.sdk.core.domain.docCategoryIdxToType
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.transferrable_objects.CheckPhotoDataTO
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import java.io.File
import java.io.IOException

class PhotoUploadFragment : ThemeWrapperFragment() {

    private var _binding: PhotoUploadFragmentBinding? = null

    private lateinit var _viewModel: PhotoUploadViewModel
    private lateinit var _docType: DocType

    private var _photo1Path: String? = null
    private var _photo2Path: String? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.takePhotoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.makeDocumentPhotoTitle.setTextColor(Color.parseColor(it))
            _binding!!.verifMethodTitle1.setTextColor(Color.parseColor(it))
            _binding!!.verifMethodTitle2.setTextColor(Color.parseColor(it))
            _binding!!.takePhotoTitle.setTextColor(Color.parseColor(it))
            _binding!!.takePhotoTitle2.setTextColor(Color.parseColor(it))
            _binding!!.backArrow.setColorFilter(Color.parseColor(it))
            _binding!!.takePhotoIcon.setColorFilter(Color.parseColor(it))
            _binding!!.takePhotoIcon2.setColorFilter(Color.parseColor(it))
            _binding!!.makePhotoButton1.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton2.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.photoUploadContinueButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundTertiaryColorHex?.let {
            _binding!!.methodCard1Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton1Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.methodCard2Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton2Background.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            _binding!!.methodCard1.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.methodCard2.setCardBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = PhotoUploadViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.photo_upload_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = PhotoUploadFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        val docTypeWithData = _viewModel.repository.getSelectedDocTypeWithData()

        if (docTypeWithData == null) {
            Toast.makeText((activity as VCheckMainActivity),
                "Error: document type & data have not been initialized.", Toast.LENGTH_LONG).show()
        } else {

        _docType = docCategoryIdxToType(docTypeWithData.category)

            _binding!!.apply {

                photoUploadContinueButton.setBackgroundColor(Color.parseColor("#BFBFBF"))
                methodCard1.isVisible = false
                methodCard2.isVisible = false
                imgPhoto1.isVisible = false
                imgPhoto2.isVisible = false
                deletePhotoButton1.isVisible = false
                deletePhotoButton2.isVisible = false

                backArrow.setOnClickListener {
                    findNavController().popBackStack()
                }

                when (_docType) {
                    DocType.FOREIGN_PASSPORT -> {
                        methodCard1.isVisible = true
                        methodCard2.isVisible = false
                        verifMethodTitle1.text =
                            getString(R.string.photo_upload_title_foreign)

                        if (docTypeWithData.country == "ua") {
                            verifMethodIcon1.setImageResource(R.drawable.doc_ua_international_passport)
                        } else {
                            verifMethodIcon1.isVisible = false
                        }
                        makePhotoButton1.setOnClickListener {
                            dispatchTakePictureIntent(1)
                        }
                    }
                    DocType.INNER_PASSPORT_OR_COMMON -> {
                        methodCard1.isVisible = true
                        methodCard2.isVisible = true
                        verifMethodIcon1.isVisible = false
                        verifMethodIcon2.isVisible = false

                        verifMethodTitle1.text =
                            getString(R.string.photo_upload_title_common_forward)
                        verifMethodTitle2.text =
                            getString(R.string.photo_upload_title_common_back)

                        makePhotoButton1.setOnClickListener {
                            dispatchTakePictureIntent(1)
                        }
                        makePhotoButton2.setOnClickListener {
                            dispatchTakePictureIntent(2)
                        }
                    }
                    DocType.ID_CARD -> {
                        methodCard1.isVisible = true
                        methodCard2.isVisible = true

                        if (docTypeWithData.country == "ua") {
                            verifMethodIcon1.isVisible = true
                            verifMethodIcon2.isVisible = true
                            verifMethodIcon1.setImageResource(R.drawable.doc_id_card_front)
                            verifMethodIcon2.setImageResource(R.drawable.doc_id_card_back)
                        } else {
                            verifMethodIcon1.isVisible = false
                            verifMethodIcon2.isVisible = false
                        }

                        verifMethodTitle1.text =
                            getString(R.string.photo_upload_title_id_card_forward)
                        verifMethodTitle2.text =
                            getString(R.string.photo_upload_title_id_card_back)

                        makePhotoButton1.setOnClickListener {
                            dispatchTakePictureIntent(1)
                        }
                        makePhotoButton2.setOnClickListener {
                            dispatchTakePictureIntent(2)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        try {
            if (resultCode == Activity.RESULT_OK) {

                var docPhotoFile: File? = null

                _binding!!.apply {
                    if (requestCode == 1) {
                        Log.i("PHOTO", "---------------- REQUEST CODE 1 | PHOTO 1 PATH: $_photo1Path")
                        imgPhoto1.isVisible = true
                        verifMethodTitle1.isVisible = false
                        verifMethodIcon1.isVisible = false
                        makePhotoButton1.isVisible = false

                        docPhotoFile = File(_photo1Path!!)
                        Picasso.get().load(docPhotoFile!!).memoryPolicy(MemoryPolicy.NO_CACHE)
                            .fit().centerInside().into(imgPhoto1)
                        deletePhotoButton1.isVisible = true
                        deletePhotoButton1.setOnClickListener {
                            imgPhoto1.isVisible = false
                            _photo1Path = null
                            deletePhotoButton1.isVisible = false
                            verifMethodTitle1.isVisible = true
                            verifMethodIcon1.isVisible = true
                            makePhotoButton1.isVisible = true
                            checkPhotoCompletenessAndSetProceedClickListener()
                        }
                    }
                    if (requestCode == 2) {
                        Log.i("PHOTO", "---------------- REQUEST CODE 2 | PHOTO 2 PATH: $_photo2Path")
                        imgPhoto2.isVisible = true
                        verifMethodTitle2.isVisible = false
                        verifMethodIcon2.isVisible = false
                        makePhotoButton2.isVisible = false

                        docPhotoFile = File(_photo2Path!!)
                        Picasso.get().load(docPhotoFile!!).memoryPolicy(MemoryPolicy.NO_CACHE)
                            .fit().centerInside().into(imgPhoto2)
                        deletePhotoButton2.isVisible = true
                        deletePhotoButton2.setOnClickListener {
                            imgPhoto2.isVisible = false
                            _photo2Path = null
                            deletePhotoButton2.isVisible = false
                            verifMethodTitle2.isVisible = true
                            verifMethodIcon2.isVisible = true
                            makePhotoButton2.isVisible = true
                            checkPhotoCompletenessAndSetProceedClickListener()
                        }
                    } else {
                        //Stub
                    }

                    if (docPhotoFile != null) {
                        checkPhotoCompletenessAndSetProceedClickListener()
                    } else {
                        Log.i("PHOTO", "BITMAP FILE IS NULL!")
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireActivity(), e.localizedMessage, Toast.LENGTH_LONG).show()
            Log.e("PHOTO_UPLOAD - ERROR", e.stackTraceToString())
        }
    }

    private fun checkPhotoCompletenessAndSetProceedClickListener() {
        if (_docType == DocType.FOREIGN_PASSPORT) {
            if (_photo1Path != null) {
                prepareForNavigation(false)
            } else {
                _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#BFBFBF"))
                _binding!!.photoUploadContinueButton.setOnClickListener {}
                Toast.makeText(activity, R.string.error_make_at_least_one_photo, Toast.LENGTH_LONG).show()
            }
        } else if (_docType == DocType.INNER_PASSPORT_OR_COMMON) {
            if (_photo1Path != null) {
                prepareForNavigation(false)
            } else if (_photo2Path != null && _photo1Path != null) {
                prepareForNavigation(true)
            } else {
                _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#BFBFBF"))
                _binding!!.photoUploadContinueButton.setOnClickListener {}
                Toast.makeText(activity, R.string.error_make_at_least_one_photo, Toast.LENGTH_LONG).show()
            }
        } else {
            if (_photo1Path != null && _photo2Path != null) {
                prepareForNavigation(true)
            } else {
                _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#BFBFBF"))
                _binding!!.photoUploadContinueButton.setOnClickListener {}
            }
        }
    }

    private fun prepareForNavigation(resetSecondPhoto: Boolean) {

        if (VCheckSDK.buttonsColorHex != null) {
            _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor(VCheckSDK.buttonsColorHex))
        } else {
            _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#2E75FF"))
        }
        if (VCheckSDK.primaryTextColorHex != null) {
            _binding!!.photoUploadContinueButton.setTextColor(Color.parseColor(VCheckSDK.primaryTextColorHex))
        } else {
            _binding!!.photoUploadContinueButton.setTextColor(Color.WHITE)
        }
        _binding!!.photoUploadContinueButton.setOnClickListener {
            val action = PhotoUploadFragmentDirections
                .actionPhotoUploadScreenToCheckPhotoFragment(
                    CheckPhotoDataTO(_docType, _photo1Path!!, _photo2Path))
            findNavController().navigate(action)

            _photo1Path = null
            if (resetSecondPhoto) {
                _photo2Path = null
            }
        }
    }

    private fun dispatchTakePictureIntent(photoIdx: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity((activity as VCheckMainActivity).packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(photoIdx)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.d("PHOTO", ex.stackTraceToString())
                    null
                }
                // Continue only if the file was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        (activity as VCheckMainActivity),
                        "com.vcheck.sdk.core",
                        it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, photoIdx)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(photoIdx: Int): File {
        val storageDir: File =
            (activity as VCheckMainActivity).getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "documentPhoto${photoIdx}", ".jpg", storageDir
        ).apply {
            if (photoIdx == 1) {
                _photo1Path = this.path
            } else {
                _photo2Path = this.path
            }
            Log.d("PHOTO", "SAVING A FILE: ${this.path}")
        }
    }
}