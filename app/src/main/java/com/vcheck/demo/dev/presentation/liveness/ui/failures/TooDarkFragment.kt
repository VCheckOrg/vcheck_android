package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.TooDarkFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity

class TooDarkFragment : Fragment(R.layout.too_dark_fragment) {

    private var _binding: TooDarkFragmentBinding? = null

    private val args: TooDarkFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = TooDarkFragmentBinding.bind(view)

        _binding!!.tooDarkRepeatButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as LivenessActivity).recreate()
        }
    }
}