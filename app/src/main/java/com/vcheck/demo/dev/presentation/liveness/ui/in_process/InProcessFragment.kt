package com.vcheck.demo.dev.presentation.liveness.ui.in_process

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.InProcessFragmentBinding
import com.vcheck.demo.dev.domain.LivenessChallengeStatus
import com.vcheck.demo.dev.domain.LivenessFailureReason
import com.vcheck.demo.dev.domain.statusCodeToLivenessChallengeStatus
import com.vcheck.demo.dev.domain.strCodeToLivenessFailureReason
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import com.vcheck.demo.dev.presentation.liveness.flow_logic.VideoProcessingListener
import com.vcheck.demo.dev.util.getFolderSizeLabel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.days

class InProcessFragment : Fragment(R.layout.in_process_fragment), VideoProcessingListener {

    private val args: InProcessFragmentArgs by navArgs()

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

        _binding!!.successButton.isVisible = false
        _binding!!.inProcessTitle.isVisible = false
        _binding!!.inProcessSubtitle.isVisible = false
        _binding!!.uploadVideoLoadingIndicator.isVisible = true

        if (args.retry) {
            onVideoProcessed((activity as LivenessActivity).videoPath!!)
        } else {
            (activity as LivenessActivity).finishLivenessSession()
            (activity as LivenessActivity).processVideoOnResult(this@InProcessFragment)
        }

        val token = ((activity as LivenessActivity).application as VcheckDemoApp)
            .appContainer.mainRepository.getVerifToken(activity as LivenessActivity)

        if (token.isNotEmpty()) {
            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                Log.d(LivenessActivity.TAG,
                    "UPL COUNTER: $lazyUploadResponseCounter")
                if (lazyUploadResponseCounter == 1 && it.data != null) {
                    Log.d(LivenessActivity.TAG, "DATA: ${it.data}")
                    if (it.data.isFinal) {
                        Toast.makeText(activity, "[TODO] This upload response is final!",
                            Toast.LENGTH_LONG).show()
                    } else if (statusCodeToLivenessChallengeStatus(it.data.status) == LivenessChallengeStatus.FAIL) {
                        if (it.data.reason != null && it.data.reason.isNotEmpty()) {
                            when(strCodeToLivenessFailureReason(it.data.reason)) {
//                                LivenessFailureReason.DISCONNECTED -> {
//                                    //TODO
//                                }
                            }
                        } else {
                            navigateToVideoUploadFailFragment()
                        }
                    } else {
                        onVideoUploadResponseSuccess()
                    }
                } else {
                    lazyUploadResponseCounter =+ 1
                }
            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
                    navigateToVideoUploadFailFragment()
                }
            }
        } else {
            Toast.makeText((activity as LivenessActivity),
                "Local(test) Liveness demo is running; skipping video upload request!", Toast.LENGTH_LONG).show()
        }
    }

    private fun onVideoUploadResponseSuccess() {
        _binding!!.uploadVideoLoadingIndicator.isVisible = false
        _binding!!.successButton.isVisible = true
        _binding!!.inProcessTitle.isVisible = true
        _binding!!.inProcessSubtitle.isVisible = true
        _binding!!.successButton.isVisible = true
        _binding!!.successButton.setOnClickListener {
            quitTestApplication()
        }
    }

    private fun navigateToVideoUploadFailFragment() {
        try {
            findNavController().navigate(R.id.action_inProcessFragment_to_failVideoUploadFragment)
        } catch (e: IllegalArgumentException) {
            Log.d(LivenessActivity.TAG,
                "Attempt of nav to success was made, but was already on another fragment")
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
                _viewModel.uploadLivenessVideo(_viewModel.repository
                    .getVerifToken(activity as LivenessActivity), partVideo)
            } else {
                findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)
            }
        }
    }

    private fun quitTestApplication() {
        (activity as LivenessActivity).finishAffinity()
        exitProcess(0)
    }
}