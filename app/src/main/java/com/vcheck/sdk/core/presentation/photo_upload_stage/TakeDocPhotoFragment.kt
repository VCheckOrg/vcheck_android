package com.vcheck.sdk.core.presentation.photo_upload_stage

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
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
import com.vcheck.sdk.core.domain.DocTypeData
import com.vcheck.sdk.core.domain.docCategoryIdxToType
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.transferrable_objects.CheckPhotoDataTO
import com.vcheck.sdk.core.presentation.transferrable_objects.PhotoUploadType
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import java.io.File

class TakeDocPhotoFragment : ThemeWrapperFragment() {

    private var _binding: PhotoUploadFragmentBinding? = null

    private lateinit var _docType: DocType

    private var _photo1Path: String? = null
    private var _photo2Path: String? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.takePhotoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.makeDocumentPhotoTitle.setTextColor(Color.parseColor(it))
            _binding!!.verifMethodTitle1.setTextColor(Color.parseColor(it))
            _binding!!.verifMethodTitle2.setTextColor(Color.parseColor(it))
            _binding!!.takePhotoTitle.setTextColor(Color.parseColor(it))
            _binding!!.takePhotoTitle2.setTextColor(Color.parseColor(it))
            _binding!!.backArrow.setColorFilter(Color.parseColor(it))
            //_binding!!.photoUploadContinueButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundTertiaryColorHex?.let {
            _binding!!.methodCard1Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton1Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.methodCard2Background.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton2Background.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
            _binding!!.methodCard1.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.methodCard2.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton1.setCardBackgroundColor(Color.parseColor(it))
            _binding!!.makePhotoButton2.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.takePhotoIcon.setColorFilter(Color.parseColor(it))
            _binding!!.takePhotoIcon2.setColorFilter(Color.parseColor(it))
        }
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

        val docTypeWithData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()

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
            }
            when (_docType) {
                DocType.FOREIGN_PASSPORT -> setUIForForeignPassport(docTypeWithData)
                DocType.INNER_PASSPORT_OR_COMMON -> setUIForInnerPassportOrCommon(docTypeWithData)
                DocType.ID_CARD -> setUIForIDCard(docTypeWithData)
            }
        }
    }

    private fun docHasOnePage(docTypeData: DocTypeData): Boolean {
        return docTypeData.maxPagesCount == 1 && docTypeData.minPagesCount == 1
    }

    private fun docHasOneRequiredPage(docTypeData: DocTypeData): Boolean {
        return docTypeData.minPagesCount == 1 && docTypeData.maxPagesCount > 1
    }

    private fun docHasTwoOrMorePagesRequired(docTypeData: DocTypeData): Boolean {
        return docTypeData.maxPagesCount > 1 && docTypeData.minPagesCount > 1
    }

    private fun setUIForIDCard(docTypeWithData: DocTypeData) {
        _binding!!.apply {
            if (docHasOnePage(docTypeWithData)) {
                methodCard1.isVisible = true
                methodCard2.isVisible = false

                if (docTypeWithData.country == "ua") {
                    verifMethodIcon1.isVisible = true
                    verifMethodIcon1.setImageResource(R.drawable.doc_id_card_front)
                } else {
                    verifMethodIcon1.isVisible = false
                }
                verifMethodTitle1.text =
                    getString(R.string.photo_upload_title_id_card_forward)

                makePhotoButton1.setOnClickListener {
                    dispatchTakePictureIntent(1)
                }
            } else {
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

    private fun setUIForInnerPassportOrCommon(docTypeWithData: DocTypeData) {
        _binding!!.apply {
            if (docHasOnePage(docTypeWithData)) {
                methodCard1.isVisible = true
                methodCard2.isVisible = false

                verifMethodIcon1.isVisible = false

                verifMethodTitle1.text =
                    getString(R.string.photo_upload_title_common_forward)

                makePhotoButton1.setOnClickListener {
                    dispatchTakePictureIntent(1)
                }
            } else {
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
        }
    }

    private fun setUIForForeignPassport(docTypeWithData: DocTypeData) {
        _binding!!.apply {
            if (docHasOnePage(docTypeWithData)) {
                methodCard1.isVisible = true
                methodCard2.isVisible = false
                verifMethodTitle1.text =
                    getString(R.string.photo_upload_title_foreign)

                if (docTypeWithData.country == "ua") {
                    verifMethodIcon1.isVisible = true
                    verifMethodIcon1.setImageResource(R.drawable.doc_ua_international_passport)
                } else {
                    verifMethodIcon1.isVisible = false
                }
                makePhotoButton1.setOnClickListener {
                    dispatchTakePictureIntent(1)
                }
            } else {
                methodCard1.isVisible = true
                methodCard2.isVisible = true

                verifMethodTitle1.text =
                    getString(R.string.photo_upload_title_common_forward)
                verifMethodTitle2.text =
                    getString(R.string.photo_upload_title_common_back)

                if (docTypeWithData.country == "ua") {
                    verifMethodIcon1.isVisible = true
                    verifMethodIcon2.isVisible = true
                    verifMethodIcon1.setImageResource(R.drawable.doc_ua_international_passport)
                    verifMethodIcon2.setImageResource(R.drawable.doc_ua_international_passport)
                } else {
                    verifMethodIcon1.isVisible = false
                    verifMethodIcon2.isVisible = false
                }
                makePhotoButton1.setOnClickListener {
                    dispatchTakePictureIntent(1)
                }
                makePhotoButton2.setOnClickListener {
                    dispatchTakePictureIntent(2)
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
                        //Log.i("PHOTO", "DOC PHOTO REQUEST CODE 1 | PHOTO 1 PATH: $_photo1Path")
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
                            makePhotoButton1.isVisible = true
                            val docTypeWithData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()
                            when(docCategoryIdxToType(docTypeWithData!!.category)) {
                                DocType.FOREIGN_PASSPORT -> {
                                    verifMethodIcon1.isVisible = true
                                    verifMethodIcon1.setImageResource(R.drawable.doc_ua_international_passport)
                                }
                                else -> {
                                    verifMethodIcon1.isVisible = false
                                }
                            }
                            checkPhotoCompletenessAndSetProceedClickListener()
                        }
                    }
                    if (requestCode == 2) {
                        //Log.i("PHOTO", "DOC PHOTO REQUEST CODE 2 | PHOTO 2 PATH: $_photo2Path")
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
                            makePhotoButton2.isVisible = true
                            val docTypeWithData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()
                            when(docCategoryIdxToType(docTypeWithData!!.category)) {
                                DocType.FOREIGN_PASSPORT -> {
                                    verifMethodIcon2.isVisible = true
                                    verifMethodIcon2.setImageResource(R.drawable.doc_ua_international_passport)
                                }
                                else -> {
                                    verifMethodIcon2.isVisible = false
                                }
                            }
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
        val docTypeWithData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()!!
        if (docHasOnePage(docTypeWithData) || docHasOneRequiredPage(docTypeWithData)) {
            if (_photo1Path != null) {
                prepareForNavigation(false)
            } else {
                _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#BFBFBF"))
                _binding!!.photoUploadContinueButton.setOnClickListener {}
                Toast.makeText(activity, R.string.error_make_at_least_one_photo, Toast.LENGTH_LONG).show()
            }
        } else if (docHasTwoOrMorePagesRequired(docTypeWithData)) {
            if (_photo2Path != null && _photo1Path != null) {
                prepareForNavigation(true)
            } else {
                _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#BFBFBF"))
                _binding!!.photoUploadContinueButton.setOnClickListener {}
                Toast.makeText(activity, R.string.error_make_two_photos, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun prepareForNavigation(resetSecondPhoto: Boolean) {
        if (VCheckSDK.designConfig!!.primary != null) {
            _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor(VCheckSDK.designConfig!!.primary))
        } else {
            _binding!!.photoUploadContinueButton.setBackgroundColor(Color.parseColor("#2E75FF"))
        }
        _binding!!.photoUploadContinueButton.setTextColor(Color.WHITE)

        _binding!!.photoUploadContinueButton.setOnClickListener {
            val action = TakeDocPhotoFragmentDirections
                .actionPhotoUploadScreenToCheckPhotoFragment(
                    CheckPhotoDataTO(_docType, _photo1Path!!, _photo2Path), PhotoUploadType.MANUAL)
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
                val photoFile = createImageFile(photoIdx)
                // Continue only if the file was successfully created
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        (activity as VCheckMainActivity),
                        "com.vcheck.sdk.core",
                        it)
                    //val photoURI: Uri = photoFile.toUri()
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, photoIdx)
                }
            }
        }
    }

    private fun createImageFile(photoIdx: Int): File {
        val file = File.createTempFile("documentPhoto${photoIdx}", ".jpg",
            (activity as VCheckMainActivity).cacheDir
        ).apply {
            if (photoIdx == 1) {
                _photo1Path = this.path
            } else {
                _photo2Path = this.path
            }
        }
        return file
    }
}