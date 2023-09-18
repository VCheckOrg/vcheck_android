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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.InProcessFragmentBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.milliseconds


class PostProcessFragment : ThemeWrapperFragment() {

    private var _binding: InProcessFragmentBinding? = null
    private lateinit var _viewModel: PostProcessViewModel

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
            _binding!!.uploadVideoLoadingIndicator.setIndicatorColor(Color.parseColor(it))
            //_binding!!.successButton.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = PostProcessViewModel(VCheckDIContainer.mainRepository)
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

        onVideoProcessed((activity as VCheckLivenessActivity).videoPath!!)

        val token = VCheckSDK.getVerificationToken()

        if (token.isNotEmpty()) {
            _viewModel.uploadResponse.observe(viewLifecycleOwner) {
                if (it != null) {
                    (requireActivity() as AppCompatActivity)
                        .checkUserInteractionCompletedForResult(it.data?.errorCode)

                    if (it.data != null) {
                        handleVideoUploadResponse(it)
                    }
                }
            }

            _viewModel.stageResponse.observe(viewLifecycleOwner) {
                if (it.data?.errorCode == null || (it.data.errorCode != null &&
                            it.data.errorCode!! > StageErrorType.VERIFICATION_NOT_INITIALIZED.toTypeIdx())) {
                    (activity as VCheckLivenessActivity).closeSDKFlow(true)
                } else {
                    Toast.makeText(requireContext(), "Unhandled end stage error", Toast.LENGTH_LONG).show()
                }
            }

            //obsolete:
//            _viewModel.stageSpecificError.observe(viewLifecycleOwner) {
//                (requireActivity() as AppCompatActivity)
//                    .checkStageErrorForResult(it?.errorData?.errorCode, executePartnerCallback = true)
//            }

            _viewModel.clientError.observe(viewLifecycleOwner) {
                if (it != null) {
                    (requireActivity() as AppCompatActivity)
                        .checkUserInteractionCompletedForResult(it.errorData?.errorCode)

                    Toast.makeText(activity, it.errorText, Toast.LENGTH_LONG).show()

                    safeNavToFailFragment(R.id.action_inProcessFragment_to_failVideoUploadFragment)
                }
            }

        } else {
            Toast.makeText((activity as VCheckLivenessActivity),
                "Token is not present!", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleVideoUploadResponse(uploadResponse: Resource<LivenessUploadResponse>) {
        if (uploadResponse.data?.data?.isFinal != null && uploadResponse.data.data.isFinal) {
            onVideoUploadResponseSuccess()
        } else {
            if (statusCodeToLivenessChallengeStatus(uploadResponse.data!!.data.status) == LivenessChallengeStatus.FAIL) {
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

    private fun onVideoUploadResponseSuccess() {
        _viewModel.getCurrentStage()
    }

    private fun onBackendObstacleMet(reason: LivenessFailureReason) {
        try {
            when(reason) {
                LivenessFailureReason.FACE_NOT_FOUND -> {
                    val action = PostProcessFragmentDirections.actionInProcessFragmentToLookStraightErrorFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.MULTIPLE_FACES -> {
                    val action = PostProcessFragmentDirections.actionInProcessFragmentToFrameInterferenceFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.FAST_MOVEMENT -> {
                    val action = PostProcessFragmentDirections.actionInProcessFragmentToTooFastMovementsFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.TOO_DARK -> {
                    val action = PostProcessFragmentDirections.actionInProcessFragmentToTooDarkFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.INVALID_MOVEMENTS -> {
                    val action = PostProcessFragmentDirections.actionInProcessFragmentToWrongMoveFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
                LivenessFailureReason.UNKNOWN -> {
                    val action = PostProcessFragmentDirections.actionInProcessFragmentToFrameInterferenceFragment()
                    action.isFromUploadResponse = true
                    findNavController().navigate(action)
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.d(VCheckLivenessActivity.TAG,
                "Attempt of nav to success was made, but was already on another fragment")
        }
    }

    private fun safeNavToFailFragment(id: Int) {
        try {
            findNavController().navigate(id)
        } catch (e: IllegalArgumentException) {
            Log.d(VCheckLivenessActivity.TAG,
                "Attempt of nav to success was made, but was already on another fragment")
        }
    }

    private fun onVideoProcessed(videoPath: String) {

        val videoFile = File(videoPath)

        Log.d(VCheckLivenessActivity.TAG, "MUXED VIDEO SIZE in kb: " + videoFile.sizeInKb)

        if (videoFile.sizeInKb > 4700) {
            compressVideoFileForResult(videoFile)
        } else {
            uploadLivenessVideo(videoFile)
        }
    }

    private fun compressVideoFileForResult(videoFile: File) {
        VideoCompressor.start(
            context = requireContext(),
            uris = listOf(videoFile.toUri()),
            isStreamable = false,
            appSpecificStorageConfiguration = AppSpecificStorageConfiguration(
                videoName = "liveness${Date().time.milliseconds}"),
            configureWith = Configuration(
                quality = VideoQuality.HIGH,
                isMinBitrateCheckEnabled = false,
                videoBitrateInMbps = 2,
                disableAudio = true,
                keepOriginalResolution = true,
            ),
            listener = object : CompressionListener {

                override fun onSuccess(index: Int, size: Long, path: String?) {
                    val compressedVideoFile = File(path!!)

                    Log.d(VCheckLivenessActivity.TAG, "COMPRESSED VIDEO SIZE: "
                            + getFolderSizeLabel(compressedVideoFile))

                    uploadLivenessVideo(compressedVideoFile)
                }
                override fun onFailure(index: Int, failureMessage: String) {
                    Log.e(VCheckLivenessActivity.TAG,
                        "VIDEO COMPRESSING FAILED: $failureMessage")
                }
                override fun onProgress(index: Int, percent: Float) {
                    // Stub
                }
                override fun onStart(index: Int) {
                    // Stub
                }
                override fun onCancelled(index: Int) {
                    // Stub
                }
            }
        )
    }

    private fun uploadLivenessVideo(videoFile: File) {
        val token = VCheckSDK.getVerificationToken()

        (activity as VCheckLivenessActivity).runOnUiThread {
            if (token.isNotEmpty()) {
                val partVideo: MultipartBody.Part =
                    MultipartBody.Part.createFormData("video.mp4", videoFile.name,
                        videoFile.asRequestBody("video/mp4".toMediaType()))
                _viewModel.uploadLivenessVideo(partVideo)
            } else {
                Toast.makeText((activity as VCheckLivenessActivity),
                    "Token is not present!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
