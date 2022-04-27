package com.vcheck.demo.dev.presentation.liveness.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.InProcessFragmentBinding
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import com.vcheck.demo.dev.presentation.liveness.flow_logic.VideoProcessingListener
import com.vcheck.demo.dev.util.getFolderSizeLabel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class InProcessFragment : Fragment(R.layout.in_process_fragment), VideoProcessingListener {

    private var _binding: InProcessFragmentBinding? = null

    private lateinit var _viewModel: InProcessViewModel

    //counting video upload chained api responses; 1st one is ping; we need 2nd to claim result
    private var lazyUploadResponseCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = InProcessViewModel(appContainer.mainRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = InProcessFragmentBinding.bind(view)

        Log.d("mux", "============================ IN PROCESS FRAGMENT START!")

        _binding!!.uploadVideoLoadingIndicator.isVisible = true

        (activity as LivenessActivity).finishLivenessSession()

        (activity as LivenessActivity).processVideoOnResult(this@InProcessFragment)

        val token = ((activity as LivenessActivity).application as VcheckDemoApp)
            .appContainer.mainRepository.getVerifToken(activity as LivenessActivity)

        if (token.isNotEmpty()) {
            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                if (lazyUploadResponseCounter == 1) {
                    //findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)
                    findNavController().navigate(R.id.action_inProcessFragment_to_successFragment)
                }
                lazyUploadResponseCounter =+ 1
            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
                    try {
                        findNavController().navigate(R.id.action_inProcessFragment_to_failVideoUploadFragment)
                    } catch (e: IllegalArgumentException) {
                        Log.d(LivenessActivity.TAG, "Attempt of nav to success was made, but was already on another fragment")
                    }
                }
            }
        } else {
            Toast.makeText((activity as LivenessActivity),
                "Local(test) Liveness demo is running; skipping video upload request!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onVideoProcessed(videoPath: String) {

        val videoFile = File(videoPath)

        Log.d("mux", getFolderSizeLabel(videoFile))

        val token = ((activity as LivenessActivity).application as VcheckDemoApp)
            .appContainer.mainRepository.getVerifToken(activity as LivenessActivity)

        (activity as LivenessActivity).runOnUiThread {
            if (token.isNotEmpty()) {
                val partVideo: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "video.mp4", videoFile.name, videoFile.asRequestBody("video/mp4".toMediaType()))

                _viewModel.uploadLivenessVideo(_viewModel.repository.getVerifToken(activity as LivenessActivity),
                    partVideo)
            } else {
//            Toast.makeText((activity as LivenessActivity),
//                "Local(test) Liveness demo is running; skipping video upload request!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)
            }
        }

    }
}