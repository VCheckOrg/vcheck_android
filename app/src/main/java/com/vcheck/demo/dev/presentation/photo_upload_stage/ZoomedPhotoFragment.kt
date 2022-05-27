package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.ZoomedPhotoFragmentBinding
import java.io.File

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
            val docPhotoFile = File(args.zoomPhotoTO.photo1Path!!)
            Picasso.get().load(docPhotoFile).memoryPolicy(MemoryPolicy.NO_CACHE)
                .fit().centerInside().into(_binding.zoomedPhoto)
        } else {
            val docPhotoFile = File(args.zoomPhotoTO.photo2Path!!)
            Picasso.get().load(docPhotoFile).memoryPolicy(MemoryPolicy.NO_CACHE)
                .fit().centerInside().into(_binding.zoomedPhoto)
        }
    }

}