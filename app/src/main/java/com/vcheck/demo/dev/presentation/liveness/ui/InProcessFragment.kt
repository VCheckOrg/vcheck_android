package com.vcheck.demo.dev.presentation.liveness.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.InProcessFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import com.vcheck.demo.dev.presentation.liveness.flow_logic.VideoProcessingListener

class InProcessFragment : Fragment(R.layout.in_process_fragment), VideoProcessingListener {

    private var _binding: InProcessFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = InProcessFragmentBinding.bind(view)

        (activity as LivenessActivity).finishLivenessSession()

        (activity as LivenessActivity).processVideoOnResult(this@InProcessFragment)
    }

    override fun onVideoProcessed() {
        findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)
    }
}