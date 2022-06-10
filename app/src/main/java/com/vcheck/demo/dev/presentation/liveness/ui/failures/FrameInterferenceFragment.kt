package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FrameInterferenceFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity

class FrameInterferenceFragment : Fragment(R.layout.frame_interference_fragment) {

    private var _binding: FrameInterferenceFragmentBinding? = null

    private val args: FrameInterferenceFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FrameInterferenceFragmentBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.frameInterferenceButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as LivenessActivity).recreate()
        }
    }
}