package com.vcheck.demo.dev.presentation.liveness.ui

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = InProcessViewModel(appContainer.mainRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = InProcessFragmentBinding.bind(view)

        _binding!!.uploadVideoLoadingIndicator.isVisible = true

        (activity as LivenessActivity).finishLivenessSession()

        (activity as LivenessActivity).processVideoOnResult(this@InProcessFragment)

        _viewModel.uploadResponse.observe(viewLifecycleOwner) {
            if (it) {
                //TODO: NO navigation on success!
                // we need to display 'Proceed' button which will restart the app/session (AKA back to partner)
                //findNavController().navigate(R.id.action_inProcessFragment_to_successFragment)
                findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)
            }
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
    }

    override fun onVideoProcessed(videoPath: String) {
        //findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)

        val videoFile = File(videoPath)

        Log.d("mux", getFolderSizeLabel(videoFile))

        val partVideo: MultipartBody.Part = MultipartBody.Part.createFormData(
            "video.mp4", videoFile.name, videoFile.asRequestBody("video/mp4".toMediaType()))

        _viewModel.uploadLivenessVideo(_viewModel.repository.getVerifToken(activity as LivenessActivity),
            partVideo)
    }
}