package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.PhotoInstructionsFragmentBinding

class PhotoInstructionsFragment : Fragment() {

    private var _binding: PhotoInstructionsFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.photo_instructions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = PhotoInstructionsFragmentBinding.bind(view)

        _binding!!.photoInstructionsButton.setOnClickListener {
            val action = PhotoInstructionsFragmentDirections.actionPhotoInstructionsFragmentToPhotoUploadScreen()
            findNavController().navigate(action)
        }
    }
}