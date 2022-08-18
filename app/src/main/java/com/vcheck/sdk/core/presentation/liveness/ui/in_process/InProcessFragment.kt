package com.vcheck.sdk.core.presentation.liveness.ui.in_process

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.InProcessFragmentBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.presentation.liveness.flow_logic.VideoProcessingListener
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import com.vcheck.sdk.core.util.getFolderSizeLabel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class InProcessFragment : ThemeWrapperFragment(), VideoProcessingListener {

    private val args: InProcessFragmentArgs by navArgs()
    private var _binding: InProcessFragmentBinding? = null
    private lateinit var _viewModel: InProcessViewModel

    //counting video upload chained api responses; 1st one is ping; we need 2nd to claim result
    private var lazyUploadResponseCounter = 0

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.successButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.inProcessBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.inProcessTitle.setTextColor(Color.parseColor(it))
            _binding!!.inProcessSubtitle.setTextColor(Color.parseColor(it))
            _binding!!.successButton.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = InProcessViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.in_process_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = InProcessFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.successButton.isVisible = false
        _binding!!.inProcessTitle.isVisible = false
        _binding!!.inProcessSubtitle.isVisible = false
        _binding!!.uploadVideoLoadingIndicator.isVisible = true

        if (args.retry) {
            onVideoProcessed((activity as VCheckLivenessActivity).videoPath!!)
        } else {
            (activity as VCheckLivenessActivity).finishLivenessSession()
            (activity as VCheckLivenessActivity).processVideoOnResult(this@InProcessFragment)
        }

        val token = VCheckSDK.getVerificationToken()

        if (token.isNotEmpty()) {
            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                if (lazyUploadResponseCounter == 1 && it != null && it.data != null) {
                    handleVideoUploadResponse(it, token)
                } else {
                    lazyUploadResponseCounter =+ 1
                }
            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                if (it != null) {
                    safeNavToFailFragment(R.id.action_inProcessFragment_to_failVideoUploadFragment)
                }
            }
        } else {
            Toast.makeText((activity as VCheckLivenessActivity),
                "Local(test) Liveness demo is running; skipping video upload request!", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleVideoUploadResponse(uploadResponse: Resource<LivenessUploadResponse>, token: String) {
            if (uploadResponse.data!!.data.isFinal) {
                onVideoUploadResponseSuccess()
            } else {
                if (statusCodeToLivenessChallengeStatus(uploadResponse.data.data.status) == LivenessChallengeStatus.FAIL) {
                    if (uploadResponse.data.data.reason != null && uploadResponse.data.data.reason.isNotEmpty()) {
                        onBackendObstacleMet(strCodeToLivenessFailureReason(uploadResponse.data.data.reason))
                    } else {
                        onVideoUploadResponseSuccess()
                    }
                } else {
                    onVideoUploadResponseSuccess()
                }
            }
    }

    private fun onBackendObstacleMet(reason: LivenessFailureReason) {
        try {
            when(reason) {
                LivenessFailureReason.FACE_NOT_FOUND -> {
                    val action = InProcessFragmentDirections.actionInProcessFragmentToLookStraightErrorFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.MULTIPLE_FACES -> {
                    val action = InProcessFragmentDirections.actionInProcessFragmentToFrameInterferenceFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.FAST_MOVEMENT -> {
                    val action = InProcessFragmentDirections.actionInProcessFragmentToTooFastMovementsFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.TOO_DARK -> {
                    val action = InProcessFragmentDirections.actionInProcessFragmentToTooDarkFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.INVALID_MOVEMENTS -> {
                    val action = InProcessFragmentDirections.actionInProcessFragmentToWrongMoveFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.UNKNOWN -> {
                    val action = InProcessFragmentDirections.actionInProcessFragmentToFrameInterferenceFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.d(VCheckLivenessActivity.TAG,
                "Attempt of nav to success was made, but was already on another fragment")
        }
    }

    private fun onVideoUploadResponseSuccess() {
        _viewModel.stageResponse.observe(viewLifecycleOwner) {
            if (it.data?.errorCode == null || it.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
                (activity as VCheckLivenessActivity).finish()
                VCheckSDK.onApplicationFinish() //!
            } else {
                Toast.makeText(activity, "Stage Error", Toast.LENGTH_LONG).show()
            }
        }
        _viewModel.getCurrentStage()
    }

    private fun safeNavToFailFragment(id: Int) {
        try {
            findNavController().navigate(id)
        } catch (e: IllegalArgumentException) {
            Log.d(VCheckLivenessActivity.TAG,
                "Attempt of nav to success was made, but was already on another fragment")
        }
    }

    override fun onVideoProcessed(videoPath: String) {

        val videoFile = File(videoPath)

        Log.d("mux", getFolderSizeLabel(videoFile))

        val token = VCheckSDK.getVerificationToken()

        (activity as VCheckLivenessActivity).runOnUiThread {
            if (token.isNotEmpty()) {
                val partVideo: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "video.mp4", videoFile.name, videoFile.asRequestBody("video/mp4".toMediaType()))
                _viewModel.uploadLivenessVideo(partVideo)
            } else {
                findNavController().navigate(R.id.action_inProcessFragment_to_livenessResultVideoViewFragment)
            }
        }
    }
}
