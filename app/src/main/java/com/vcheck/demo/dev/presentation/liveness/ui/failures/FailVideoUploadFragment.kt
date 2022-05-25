package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentFailVideoUploadBinding

class FailVideoUploadFragment : Fragment() {

    private var _binding: FragmentFailVideoUploadBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fail_video_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFailVideoUploadBinding.bind(view)

        _binding!!.contactSupportButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = ClipDescription.MIMETYPE_TEXT_PLAIN
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("info@vycheck.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT,"VCheck support")
            intent.putExtra(Intent.EXTRA_TEXT, "Problem report")
            startActivity(Intent.createChooser(intent,"Send Email"))
        }

        _binding!!.retryButton.setOnClickListener {
            val action = FailVideoUploadFragmentDirections.actionFailVideoUploadFragmentToInProcessFragment2()
            action.retry = true
            findNavController().navigate(action)
        }
    }
}