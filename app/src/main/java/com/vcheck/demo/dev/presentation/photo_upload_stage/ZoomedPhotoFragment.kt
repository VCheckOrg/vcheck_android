package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.ZoomedPhotoFragmentBinding
import com.vcheck.demo.dev.util.ThemeWrapperFragment
import java.io.File

class ZoomedPhotoFragment : ThemeWrapperFragment() {

    private val args: ZoomedPhotoFragmentArgs by navArgs()
    private lateinit var _binding: ZoomedPhotoFragmentBinding

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.vcheckBackgroundPrimaryColorHex?.let {
            _binding.zoomedPhotoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.textColorHex?.let {
            _binding.crossIcon.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.zoomed_photo_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ZoomedPhotoFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

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