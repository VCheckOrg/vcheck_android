package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.PhotoUploadFragmentBinding
import com.vcheck.demo.dev.domain.DocType
import com.vcheck.demo.dev.domain.docCategoryIdxToType
import com.vcheck.demo.dev.presentation.transferrable_objects.CheckPhotoDataTO
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


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

        _docType = docCategoryIdxToType(_viewModel.repository.getSelectedDocTypeWithData().category)

        _binding!!.apply {

            photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_inactive_button)
            methodCard1.isVisible = false
            methodCard2.isVisible = false
            imgPhoto1.isVisible = false
            imgPhoto2.isVisible = false

            when (_docType) {
                DocType.FOREIGN_PASSPORT -> {
                    methodCard1.isVisible = true
                    methodCard2.isVisible = false
                    verifMethodTitle1.text =
                        getString(R.string.photo_upload_title_foreign)
                    makePhotoButton1.setOnClickListener {
                        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1)
                    }
                }
                DocType.INNER_PASSPORT_OR_COMMON -> {
                    methodCard1.isVisible = true
                    methodCard2.isVisible = true
                    verifMethodTitle1.text =
                        getString(R.string.photo_upload_title_common_forward)
                    verifMethodTitle2.text =
                        getString(R.string.photo_upload_title_common_back)
                    makePhotoButton1.setOnClickListener {
                        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1)
                    }
                    makePhotoButton2.setOnClickListener {
                        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 2)
                    }
                }
                DocType.ID_CARD -> {
                    methodCard1.isVisible = true
                    methodCard2.isVisible = true
                    verifMethodTitle1.text =
                        getString(R.string.photo_upload_title_id_card_forward)
                    verifMethodTitle2.text =
                        getString(R.string.photo_upload_title_id_card_back)
                    makePhotoButton1.setOnClickListener {
                        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1)
                    }
                    makePhotoButton1.setOnClickListener {
                        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 2)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {

            val docPhotoBitmap: Bitmap? = data?.getParcelableExtra("data")

            //TODO Multipart

            _binding!!.apply {
                if (requestCode == 1) {
                    imgPhoto1.isVisible = true
                    verifMethodTitle1.isVisible = false
                    verifMethodIcon1.isVisible = false
                    makePhotoButton1.isVisible = false
                    imgPhoto1.setImageBitmap(docPhotoBitmap)
                }
                if (requestCode == 2) {
                    imgPhoto2.isVisible = true
                    verifMethodTitle2.isVisible = false
                    verifMethodIcon2.isVisible = false
                    makePhotoButton2.isVisible = false
                    imgPhoto2.setImageBitmap(docPhotoBitmap)
                } else {
                    //Stub
                }
                val file = docPhotoBitmap?.let { bitmapToFile(it, requestCode) }
                if (file != null) {
                    checkPhotoCompletenessAndSetProceedClickListener()
                }
            }
        }
    }

    private fun checkPhotoCompletenessAndSetProceedClickListener() {
        if (_docType == DocType.FOREIGN_PASSPORT) {
            if (_photo1Path != null) {
                _binding!!.photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_blue_button)
                _binding!!.photoUploadContinueButton.setTextColor(Color.WHITE)
                _binding!!.photoUploadContinueButton.setOnClickListener {
                    val action = PhotoUploadFragmentDirections
                        .actionPhotoUploadScreenToCheckPhotoFragment(
                            CheckPhotoDataTO(_docType, _photo1Path!!, null))
                    findNavController().navigate(action)
                }
            } else {
                Toast.makeText(activity, "Please make the photo first", Toast.LENGTH_LONG).show()
            }
        } else {
            if (_photo1Path != null && _photo2Path != null) {
                _binding!!.photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_blue_button)
                _binding!!.photoUploadContinueButton.setTextColor(Color.WHITE)
                _binding!!.photoUploadContinueButton.setOnClickListener {
                    val action = PhotoUploadFragmentDirections
                        .actionPhotoUploadScreenToCheckPhotoFragment(
                            CheckPhotoDataTO(_docType, _photo1Path!!, _photo2Path!!)
                        )
                    findNavController().navigate(action)
                }
            } else {
                Toast.makeText(activity, "Please make all photos first", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap, requestCode: Int): File? {
        var file: File? = null
        try {
            file = File(
                Environment.getDataDirectory()
                    .toString() + File.separator + "documentPhoto"
            )
            file.createNewFile()

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val byteArray = bos.toByteArray()

            val fos = FileOutputStream(file)
            fos.write(byteArray)
            fos.flush()
            fos.close()

            if (requestCode == 1) {
                _photo1Path = file.path
            }
            if (requestCode == 2) {
                _photo2Path = file.path
            }

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}