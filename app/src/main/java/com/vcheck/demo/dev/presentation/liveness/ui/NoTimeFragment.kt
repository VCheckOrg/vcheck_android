package com.vcheck.demo.dev.presentation.liveness.ui

import android.os.Bundle
import android.view.View
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

        (activity as LivenessActivity).finishLivenessSession()

        _binding!!.tryAgainButton.setOnClickListener {
            (activity as LivenessActivity).resetMilestonesForNewLivenessSession()
            (activity as LivenessActivity).resetUIForNewLivenessSession()
            findNavController().popBackStack()
        }

        _binding!!.noTimeCorrectTextButton.setOnClickListener {
            //TODO
        }
    }
}