package com.vcheck.sdk.core.presentation.doc_photo_manual

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentZoomedDocPhotoBinding
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import java.io.File

class ZoomedPhotoFragment : ThemeWrapperFragment() {

    private val args: ZoomedPhotoFragmentArgs by navArgs()
    private lateinit var _binding: FragmentZoomedDocPhotoBinding

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding.zoomedPhotoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding.crossIcon.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_zoomed_doc_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentZoomedDocPhotoBinding.bind(view)

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