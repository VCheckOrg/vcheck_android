package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.WrongMoveFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity

class WrongMoveFragment : Fragment(R.layout.wrong_move_fragment)  {

    private var _binding: WrongMoveFragmentBinding? = null

    private val args: WrongMoveFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = WrongMoveFragmentBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.wrongMoveRepeatButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as LivenessActivity).recreate()
        }
    }
}