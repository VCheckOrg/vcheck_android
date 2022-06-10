package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.NoTimeFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity

class NoTimeFragment : Fragment(R.layout.no_time_fragment) {

    private var _binding: NoTimeFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = NoTimeFragmentBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        (activity as LivenessActivity).finishLivenessSession()

        _binding!!.tryAgainButton.setOnClickListener {
            findNavController().popBackStack()
            (activity as LivenessActivity).recreate()
        }
    }
}