package com.vcheck.demo.dev.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.ErrorFragmentBinding

class ErrorFragment : Fragment(R.layout.error_fragment) {

    private var _binding: ErrorFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ErrorFragmentBinding.bind(view)

        _binding!!.errorButton.setOnClickListener {
            //here nav logic may be more complex
            findNavController().popBackStack()
        }
    }
}