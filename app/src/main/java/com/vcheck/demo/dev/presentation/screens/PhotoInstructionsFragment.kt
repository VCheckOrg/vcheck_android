package com.vcheck.demo.dev.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.PhotoInstructionsFragmentBinding
import com.vcheck.demo.dev.domain.DocType

class PhotoInstructionsFragment : Fragment() {

    private var _binding: PhotoInstructionsFragmentBinding? = null
    private val args: PhotoInstructionsFragmentArgs by navArgs()

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

            val docMethod = args.docTypeTO.docType

            if (docMethod == DocType.INNER_PASSPORT || docMethod == DocType.FOREIGN_PASSPORT) {
                val action =
                    PhotoInstructionsFragmentDirections.actionPhotoInstructionsFragmentToPhotoUploadScreen()
                findNavController().navigate(action)
            } else {
                val action =
                    PhotoInstructionsFragmentDirections.actionPhotoInstructionsFragmentToIDCardPhotoUploadFragment()
                findNavController().navigate(action)
            }
        }
    }
}