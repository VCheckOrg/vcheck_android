package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.LookStraightErrorFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity

class LookStraightErrorFragment : Fragment(R.layout.look_straight_error_fragment) {

    private var _binding: LookStraightErrorFragmentBinding? = null

    private val args: LookStraightErrorFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = LookStraightErrorFragmentBinding.bind(view)

        _binding!!.lookStratightErrorButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as LivenessActivity).recreate()
        }
    }
}