package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.PhotoUploadFragmentBinding
import com.vcheck.demo.dev.domain.DocType
import com.vcheck.demo.dev.domain.docCategoryIdxToType
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.transferrable_objects.CheckPhotoDataTO
import java.io.File
import java.io.IOException

class PhotoUploadFragment : Fragment() {

    private var _binding: PhotoUploadFragmentBinding? = null

    private lateinit var _viewModel: PhotoUploadViewModel

    private lateinit var _docType: DocType

    private var _photo1Path: String? = null
    private var _photo2Path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel =
            PhotoUploadViewModel(appContainer.mainRepository)
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

        val docTypeWithData = _viewModel.repository.getSelectedDocTypeWithData()
        _docType = docCategoryIdxToType(docTypeWithData.category)

        _binding!!.apply {

            photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_inactive_button)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {

            var docPhotoBitmap: Bitmap? = null

            _binding!!.apply {
                if (requestCode == 1) {
                    Log.i("PHOTO", "---------------- REQUEST CODE 1")
                    imgPhoto1.isVisible = true
                    verifMethodTitle1.isVisible = false
                    verifMethodIcon1.isVisible = false
                    makePhotoButton1.isVisible = false

                    docPhotoBitmap = BitmapFactory.decodeFile(_photo1Path!!)
                    imgPhoto1.setImageBitmap(docPhotoBitmap)
                    deletePhotoButton1.isVisible = true
                    deletePhotoButton1.setOnClickListener {
                        imgPhoto1.isVisible = false
                        _photo1Path = null
                        deletePhotoButton1.isVisible = false
                        verifMethodTitle1.isVisible = true
                        verifMethodIcon1.isVisible = true
                        makePhotoButton1.isVisible = true
                        photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_inactive_button)
                        photoUploadContinueButton.setOnClickListener {}
                    }
                }
                if (requestCode == 2) {
                    Log.i("PHOTO", "---------------- REQUEST CODE 2")
                    imgPhoto2.isVisible = true
                    verifMethodTitle2.isVisible = false
                    verifMethodIcon2.isVisible = false
                    makePhotoButton2.isVisible = false

                    docPhotoBitmap = BitmapFactory.decodeFile(_photo2Path!!)
                    imgPhoto2.setImageBitmap(docPhotoBitmap)
                    deletePhotoButton2.isVisible = true
                    deletePhotoButton2.setOnClickListener {
                        imgPhoto2.isVisible = false
                        _photo2Path = null
                        deletePhotoButton2.isVisible = false
                        verifMethodTitle2.isVisible = true
                        verifMethodIcon2.isVisible = true
                        makePhotoButton2.isVisible = true
                        photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_inactive_button)
                        photoUploadContinueButton.setOnClickListener {}
                    }
                } else {
                    //Stub
                }

                if (docPhotoBitmap != null) {
                    checkPhotoCompletenessAndSetProceedClickListener()
                } else {
                    Log.i("PHOTO", "BITMAP FILE IS NULL!")
                }
            }
        }
    }

    private fun checkPhotoCompletenessAndSetProceedClickListener() {
        if (_docType == DocType.FOREIGN_PASSPORT || _docType == DocType.INNER_PASSPORT_OR_COMMON) {
            if (_photo1Path != null) {
                _binding!!.photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_blue_button)
                _binding!!.photoUploadContinueButton.setTextColor(Color.WHITE)
                _binding!!.photoUploadContinueButton.setOnClickListener {
                    val action = PhotoUploadFragmentDirections
                        .actionPhotoUploadScreenToCheckPhotoFragment(
                            CheckPhotoDataTO(_docType, _photo1Path!!, null)
                        )
                    findNavController().navigate(action)

                    _photo1Path = null

                }
            } else {
                Toast.makeText(activity, R.string.error_make_at_least_one_photo, Toast.LENGTH_LONG).show()
            }
        } else {
            if (_photo1Path != null && _photo2Path != null) {
                _binding!!.photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_blue_button)
                _binding!!.photoUploadContinueButton.setTextColor(Color.WHITE)
                _binding!!.photoUploadContinueButton.setOnClickListener {
                    val action = PhotoUploadFragmentDirections
                        .actionPhotoUploadScreenToCheckPhotoFragment(
                            CheckPhotoDataTO(_docType, _photo1Path!!, _photo2Path!!))
                    findNavController().navigate(action)

                    _photo1Path = null
                    _photo2Path = null
                }
            }
//            if (_photo1Path == null || _photo2Path == null) {
//                Toast.makeText(activity, "Требуется сдедать оба фото документа", Toast.LENGTH_LONG).show()
//            }
        }
    }

    private fun dispatchTakePictureIntent(photoIdx: Int) {
        Log.d("PHOTO", "------------ dispatchTakePictureIntent(photoIdx: $photoIdx)")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            //startActivity(takePictureIntent)
            takePictureIntent.resolveActivity((activity as MainActivity).packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(photoIdx)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.d("PHOTO", ex.stackTraceToString())
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        (activity as MainActivity),
                        "com.vcheck.demo.dev",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, photoIdx)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(photoIdx: Int): File {
        val storageDir: File =
            (activity as MainActivity).getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
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