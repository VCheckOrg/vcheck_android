package com.vcheck.sdk.core.presentation.liveness.ui.in_process

import android.content.Intent
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
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import com.vcheck.sdk.core.util.checkUserInteractionCompletedForResult
import com.vcheck.sdk.core.util.getFolderSizeLabel
import com.vcheck.sdk.core.util.sizeInKb
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.milliseconds


class InProcessFragment : ThemeWrapperFragment() {

    private var _binding: InProcessFragmentBinding? = null
    private lateinit var _viewModel: InProcessViewModel

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

            _viewModel.clientError.observe(viewLifecycleOwner) {
                if (it != null) {
                    (requireActivity() as AppCompatActivity)
                        .checkUserInteractionCompletedForResult(it.errorData?.errorCode)

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
            (requireActivity() as AppCompatActivity)
                .checkUserInteractionCompletedForResult(it.data?.errorCode)

            if (it.data?.errorCode == null || it.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
                (activity as VCheckLivenessActivity).closeSDKFlow(true)
            } else if (it.data.errorCode != null
                && it.data.errorCode == StageObstacleErrorType.VERIFICATION_EXPIRED.toTypeIdx()) {
                Toast.makeText(requireContext(), R.string.verification_expired, Toast.LENGTH_LONG).show()
                closeSDKFlow(shouldExecuteEndCallback = false)
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

    private fun closeSDKFlow(shouldExecuteEndCallback: Boolean) {
        (VCheckDIContainer).mainRepository.setFirePartnerCallback(shouldExecuteEndCallback)
        (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        val intents = Intent(requireActivity(), VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}
