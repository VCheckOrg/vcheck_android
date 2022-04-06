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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.PhotoUploadFragmentBinding
import com.vcheck.demo.dev.presentation.MainActivity
import okhttp3.MultipartBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class PhotoUploadFragment : Fragment() {

    private var _binding: PhotoUploadFragmentBinding? = null
    private lateinit var _viewModel: PhotoUploadViewModel
    private lateinit var _image: MultipartBody.Part

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = PhotoUploadViewModel(appContainer.mainRepository)
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

        _binding!!.makePhotoButton.setOnClickListener {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1)
        }

        _binding!!.photoUploadContinueButton.setOnClickListener {
            _viewModel.uploadVerificationDocument(
                _viewModel.repository.getVerifToken(activity as MainActivity),
                _image
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            val passportImageBitmap: Bitmap? = data?.getParcelableExtra("data")

            val file = passportImageBitmap?.let { bitmapToFile(it) }

            //Multipart

            _binding!!.apply {
                passportFullImage.setImageBitmap(passportImageBitmap)
                passportImage.isVisible = false
                passportTitle.isVisible = false
                makePhotoButton.isVisible = false
                photoUploadContinueButton.setBackgroundResource(R.drawable.shape_for_blue_button)
                photoUploadContinueButton.setTextColor(Color.WHITE)
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File? {
        var file: File? = null
        return try {
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
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }
}

