package com.vcheck.demo.dev.presentation.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.PhotoUploadFragmentBinding

class PhotoUploadFragment : Fragment() {

    private var _binding: PhotoUploadFragmentBinding? = null

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val passportImageBitmap: Bitmap? = data?.getParcelableExtra<Bitmap>("data")

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
}
