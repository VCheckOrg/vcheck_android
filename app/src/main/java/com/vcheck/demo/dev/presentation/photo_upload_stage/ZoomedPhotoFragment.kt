package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.ZoomedPhotoFragmentBinding

class ZoomedPhotoFragment : Fragment(R.layout.zoomed_photo_fragment) {

    private val args: ZoomedPhotoFragmentArgs by navArgs()
    private lateinit var _binding: ZoomedPhotoFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ZoomedPhotoFragmentBinding.bind(view)

        _binding.crossIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        if (args.zoomPhotoTO.photo2Path == null) {
            val docImage1File = BitmapFactory.decodeFile(args.zoomPhotoTO.photo1Path)
            _binding.zoomedPhoto.setImageBitmap(docImage1File)
        } else {
            val docImage2File = BitmapFactory.decodeFile(args.zoomPhotoTO.photo2Path)
            _binding.zoomedPhoto.setImageBitmap(docImage2File)
        }

    }

}