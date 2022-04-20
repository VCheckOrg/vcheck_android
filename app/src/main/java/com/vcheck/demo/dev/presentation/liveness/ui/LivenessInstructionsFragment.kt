package com.vcheck.demo.dev.presentation.liveness.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.LivenessInstructionsFragmentBinding

class LivenessInstructionsFragment : Fragment(R.layout.liveness_instructions_fragment) {

    private var _binding: LivenessInstructionsFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = LivenessInstructionsFragmentBinding.bind(view)

        _binding!!.livenessStartButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_faceCheckFragment)
        }
    }
}