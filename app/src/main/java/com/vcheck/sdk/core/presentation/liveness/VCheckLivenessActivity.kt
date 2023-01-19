package com.vcheck.sdk.core.presentation.liveness

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraCharacteristics
import android.os.*
import android.util.Log
import android.util.Size
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ActivityVcheckLivenessBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.LivenessGestureResponse
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.presentation.liveness.flow_logic.*
import com.vcheck.sdk.core.util.VCheckContextUtils
import com.vcheck.sdk.core.util.images.BitmapUtils
import com.vcheck.sdk.core.util.setMargins
import com.vcheck.sdk.core.util.sizeInKb
import com.vcheck.sdk.core.util.vibrateDevice
import com.vcheck.sdk.core.util.video.Muxer
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


@OptIn(DelicateCoroutinesApi::class)
class VCheckLivenessActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LivenessActivity"
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 15000 //max is 15000
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 800 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 150 //may reduce a bit
        private const val IMAGE_CAPTURE_DEBOUNCE_MILLIS: Long = 50 //may increase a bit
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
        private const val FULL_HCF_VIDEO_STREAM_WIDTH_LIMIT = 800
        private const val FULL_HCF_VIDEO_STREAM_HEIGHT_LIMIT = 800
        private const val LIMITED_HCF_VIDEO_STREAM_WIDTH_LIMIT = 640
        private const val LIMITED_HCF_VIDEO_STREAM_HEIGHT_LIMIT = 640
    }

    private lateinit var binding: ActivityVcheckLivenessBinding

    private val scope = CoroutineScope(newSingleThreadContext("liveness"))

    private val imageCaptureExecutor = Executors.newCachedThreadPool()

    var apiRequestTimer: Timer? = null

    var takeImageTimer: Timer? = null

    var mToast: Toast? = null

    var streamSize: Size? = null

    var imageCapture: ImageCapture? = null

    // -- For HardwareCapabilityFlow.FULL:
    var fullHCFVideoCapture: VideoCapture<VideoOutput>? = null
    var fullHCFRecording: Recording? = null

    // -- For HardwareCapabilityFlow.LIMITED:
    var limitedHCFBitmapList: ArrayList<Bitmap>? = ArrayList()
    var limitedHCFMuxer: Muxer? = null

    var videoPath: String? = null
    var hardwareCapabilityFlow: HardwareCapabilityFlow = HardwareCapabilityFlow.LIMITED

    var isLivenessSessionFinished: Boolean = false
    private var livenessSessionLimitCheckTime: Long = 0
    private var blockProcessingByUI: Boolean = false
    private var blockRequestByProcessing: Boolean = false

    private var gestureCheckBitmap: Bitmap? = null

    private var milestoneFlow: StandardMilestoneFlow =
        StandardMilestoneFlow()

    private fun changeColorsToCustomIfPresent() {
        val drawable = binding.cosmeticRoundedFrame.background as GradientDrawable
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.livenessActivityBackground.setBackgroundColor(Color.parseColor(it))
            drawable.setColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.backArrow.setColorFilter(Color.parseColor(it))
            binding.popSdkTitle.setTextColor(Color.parseColor(it))
            binding.checkFaceTitle.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            drawable.setStroke(7, Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVcheckLivenessBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        onBackPressedDispatcher.addCallback {
            //Stub; no back press needed throughout liveness flow
        }

        changeColorsToCustomIfPresent()

        setHeader()

        setupFlowForNewLivenessSession()

        setMilestones()

        initSetupUI()

        indicateNextMilestone(milestoneFlow.getFirstStage(), true)

        setCameraProviderListener()
    }

    @SuppressLint("RestrictedApi")
    private fun setCameraProviderListener() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this@VCheckLivenessActivity)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                setHCF(cameraProvider)

                buildImageCaptureUseCase()

                if (!isLimitedHardwareFlow()) {
                    buildVideoCaptureUseCase()
                    setupVideoRecording()
                }
                bindPreview(cameraProvider)
                setTakeImageTimer()

            } catch (e: ExecutionException) {
                showSingleToast("Error while setting camera provider: ${e.message}")
                e.printStackTrace()
            } catch (e: InterruptedException) {
                showSingleToast("Error while setting camera provider: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                showSingleToast("Unidentified Error while setting camera provider: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this@VCheckLivenessActivity))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        val preview: Preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
        try {
            binding.cameraPreviewView.viewPort?.let {
                val useCaseGroup = if (isLimitedHardwareFlow())
                    UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageCapture!!)
                        .setViewPort(it)
                        .build()
                 else UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .addUseCase(fullHCFVideoCapture!!)
                    .setViewPort(it)
                    .build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@VCheckLivenessActivity,
                    cameraSelector, useCaseGroup)
            }
        } catch (e: NullPointerException) {
            showSingleToast("Image Capture or View Port have not been initialized in time")
        } catch (e: Exception) {
            showSingleToast("Error while building preview: ${e.message}")
        }
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    fun setHCF(cameraProvider: ProcessCameraProvider) {
        hardwareCapabilityFlow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val level = CameraSelector.DEFAULT_BACK_CAMERA
                .filter(cameraProvider.availableCameraInfos)
                .firstOrNull()
                ?.let { Camera2CameraInfo.from(it) }
                ?.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            Log.d(TAG, "DEVICE CAMERA LEVEL: $level")
            if (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                HardwareCapabilityFlow.LIMITED
            } else {
                HardwareCapabilityFlow.FULL
            }
        } else {
            HardwareCapabilityFlow.LIMITED
        }
    }

    @SuppressLint("RestrictedApi")
    private fun buildImageCaptureUseCase() {
        val resMaxSize = if (isLimitedHardwareFlow()) {
            Size(LIMITED_HCF_VIDEO_STREAM_WIDTH_LIMIT,
                LIMITED_HCF_VIDEO_STREAM_HEIGHT_LIMIT)
        } else {
            Size(FULL_HCF_VIDEO_STREAM_WIDTH_LIMIT,
                FULL_HCF_VIDEO_STREAM_HEIGHT_LIMIT)
        }
        imageCapture = ImageCapture.Builder()
            .setBufferFormat(ImageFormat.YUV_420_888)
            .setMaxResolution(resMaxSize)
            .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
            .setJpegQuality(70)
            .setIoExecutor(imageCaptureExecutor)
            .build()
    }

    private fun setMilestones() {
        val milestonesList = VCheckDIContainer.mainRepository.getLivenessMilestonesList()
        if (milestonesList != null) {
            milestoneFlow.setStagesList(milestonesList)
        } else {
            showSingleToast("Dynamic milestone list not found: probably, milestone list was not " +
                    "retrieved from verification service or not cached properly.")
        }
    }

    private fun setupFlowForNewLivenessSession() {
        apiRequestTimer?.cancel()
        takeImageTimer?.cancel()
        fullHCFRecording?.close()
        fullHCFRecording = null
        imageCapture = null
        fullHCFVideoCapture = null
        milestoneFlow.resetStages()
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        isLivenessSessionFinished = false
        setGestureRequestDebounceTimer()
    }

    private fun setTakeImageTimer() {
        takeImageTimer = fixedRateTimer("liveness_image_capture_timer", false,
            0L, IMAGE_CAPTURE_DEBOUNCE_MILLIS) {
            imageCapture?.takePicture(imageCaptureExecutor, object: ImageCapture.OnImageCapturedCallback() {
                @ExperimentalGetImage
                override fun onCaptureSuccess(image: ImageProxy) {
                    // Use the image, then make sure to close it.
                    //Log.d(TAG, "GOT PICTURE: W - ${image.width} | H - ${image.height}")
                    if (image.width != streamSize?.width || image.height != streamSize?.height) {
                        streamSize = Size(image.width, image.height)
                    }
                    if (!isLivenessSessionFinished) {
                        val bitmap = BitmapUtils.getBitmap(image)!!
                        gestureCheckBitmap = bitmap
                        if (isLimitedHardwareFlow()) {
                            limitedHCFBitmapList?.add(bitmap)
                        }
                    }
                    image.close()
                }
                override fun onError(exception: ImageCaptureException) {
                    val errorType = exception.imageCaptureError
                    Log.d(TAG, "IMG CAPTURE - TAKE PICTURE ERROR: $errorType")
                }
            })
        }
    }

    fun isLimitedHardwareFlow(): Boolean {
        return hardwareCapabilityFlow == HardwareCapabilityFlow.LIMITED
    }

    private fun setGestureRequestDebounceTimer() {
        apiRequestTimer = fixedRateTimer("liveness_api_request_timer", false,
            0L, GESTURE_REQUEST_DEBOUNCE_MILLIS) {
            scope.launch {
                determineImageResult()
            }
        }
    }

    fun finishLivenessSession() {
        apiRequestTimer?.cancel()
        takeImageTimer?.cancel()
        fullHCFRecording?.close()
        fullHCFRecording = null
        imageCapture = null
        fullHCFVideoCapture = null
        limitedHCFBitmapList = ArrayList()
        limitedHCFMuxer = null
        isLivenessSessionFinished = true
        gestureCheckBitmap = null
    }

    private fun enoughTimeForNextGesture(): Boolean {
        return SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime <= LIVENESS_TIME_LIMIT_MILLIS
    }

    private suspend fun determineImageResult() {
        if (!isLivenessSessionFinished
            && !blockProcessingByUI
            && !blockRequestByProcessing
            && enoughTimeForNextGesture()) {
            if (gestureCheckBitmap != null) {
                blockRequestByProcessing = true

                val file = File(createTempFileForBitmapFrame(gestureCheckBitmap!!))

                val image: MultipartBody.Part = try {

                    val initSizeKb = file.sizeInKb
                    if (initSizeKb < 95.0) {
                        MultipartBody.Part.createFormData(
                            "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                    } else {
                        val compressedImageFile = Compressor.compress(this@VCheckLivenessActivity, file) {
                            destination(file)
                            size(95_000, stepSize = 30, maxIteration = 10)
                        }
                        MultipartBody.Part.createFormData("image.jpg", compressedImageFile.name,
                            compressedImageFile.asRequestBody("image/jpeg".toMediaType()))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Exception while compressing Liveness frame image. " +
                            "Attempting to send default frame | \n${e.printStackTrace()}")
                    MultipartBody.Part.createFormData(
                        "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                } catch (e: Error) {
                    Log.w(TAG, "Error while compressing Liveness frame image. " +
                            "Attempting to send default frame | \n${e.printStackTrace()}")
                    MultipartBody.Part.createFormData(
                        "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                }

                val currentGesture = milestoneFlow.getGestureRequestFromCurrentStage()
                val response = VCheckDIContainer.mainRepository.sendLivenessGestureAttempt(
                    image, MultipartBody.Part.createFormData("gesture", currentGesture))

                if (response != null) {
                    processCheckResult(response)
                } else {
                    blockRequestByProcessing = false
                    Log.d(TAG, "Liveness: response for current index not containing data! Max image size may be exceeded")
                }
            }
        } else {
            if (!enoughTimeForNextGesture()) {
                runOnUiThread {
                    onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_noTimeFragment)
                }
            }
        }
    }

    private fun processCheckResult(it: LivenessGestureResponse) {
        Log.d(TAG, "RESPONSE: ${it.success} | ${it.message} | ${it.errorCode}")
        blockRequestByProcessing = false
        runOnUiThread {
            if (!isLivenessSessionFinished) {
                if (milestoneFlow.areAllStagesPassed()) {
                    if (!isLimitedHardwareFlow()) {
                        processFullHCFVideoOnResult()
                    } else {
                        processLimitedHCFVideoOnResult()
                    }
                } else {
                    val currentStage = milestoneFlow.getCurrentStage()
                    if (it.success && currentStage != null) {
                        milestoneFlow.incrementCurrentStage()
                        val nextStage = milestoneFlow.getCurrentStage()
                        if (nextStage != null) {
                            blockProcessingByUI = true
                            indicateNextMilestone(nextStage, false)
                        }
                    }
                    if (it.errorCode != 0) {
                        showSingleToast("GESTURE CHECK ERROR: [${it.errorCode}]")
                    }
                }
            }
        }
    }

    /// -------------------------------------------- UI functions

    private fun initSetupUI() {
        binding.arrowAnimationView.isVisible = false
        binding.faceAnimationView.isVisible = false
        binding.stageSuccessAnimBorder.isVisible = false
        binding.checkFaceTitle.text = getString(R.string.wait_for_liveness_start)
        binding.imgViewStaticStageIndication.isVisible = false
    }

    private fun indicateNextMilestone(nextMilestoneType: GestureMilestoneType,
                                      indicateStageAsInitial: Boolean) {

        binding.faceAnimationView.isVisible = false
        binding.arrowAnimationView.isVisible = false

        if (!indicateStageAsInitial) {
            vibrateDevice(this@VCheckLivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
            binding.imgViewStaticStageIndication.isVisible = true
            binding.stageSuccessAnimBorder.isVisible = true
            animateStageSuccessFrame()
            Handler(Looper.getMainLooper()).postDelayed ({
                updateUIOnMilestoneSuccess(nextMilestoneType)
            }, BLOCK_PIPELINE_TIME_MILLIS)
        } else {
            updateUIOnMilestoneSuccess(nextMilestoneType)
        }
    }

    private fun updateUIOnMilestoneSuccess(nextMilestoneType: GestureMilestoneType) {

        binding.imgViewStaticStageIndication.isVisible = false
        binding.faceAnimationView.cancelAnimation()
        val faceAnimeRes = when(nextMilestoneType) {
            GestureMilestoneType.UpHeadPitchMilestone -> R.raw.up
            GestureMilestoneType.DownHeadPitchMilestone -> R.raw.down
            GestureMilestoneType.OuterRightHeadYawMilestone -> R.raw.right
            GestureMilestoneType.OuterLeftHeadYawMilestone -> R.raw.left
            GestureMilestoneType.MouthOpenMilestone -> R.raw.mouth
            else -> R.raw.face_plus_phone
        }
        binding.faceAnimationView.isVisible = true
        if (nextMilestoneType == GestureMilestoneType.StraightHeadCheckMilestone) {
            binding.faceAnimationView.scaleX = 1F
            binding.faceAnimationView.scaleY = 1F
        } else {
            binding.faceAnimationView.scaleX = 2F
            binding.faceAnimationView.scaleY = 2F
        }
        binding.faceAnimationView.setAnimation(faceAnimeRes)
        binding.faceAnimationView.playAnimation()

        when (nextMilestoneType) {
            GestureMilestoneType.OuterLeftHeadYawMilestone -> {
                binding.arrowAnimationView.isVisible = true
                binding.arrowAnimationView.setMargins(null, null,
                    300, null)
                binding.arrowAnimationView.rotation = 0F
                binding.arrowAnimationView.playAnimation()
            }
            GestureMilestoneType.OuterRightHeadYawMilestone -> {
                binding.arrowAnimationView.isVisible = true
                binding.arrowAnimationView.setMargins(null, null,
                    -300, null)
                binding.arrowAnimationView.rotation = 180F
                binding.arrowAnimationView.playAnimation()
            }
            GestureMilestoneType.UpHeadPitchMilestone -> {
                binding.arrowAnimationView.isVisible = true
                binding.arrowAnimationView.setMargins(0, 0,
                    0, 0)
                binding.arrowAnimationView.rotation = 90F
                binding.arrowAnimationView.playAnimation()
            }
            GestureMilestoneType.DownHeadPitchMilestone -> {
                binding.arrowAnimationView.isVisible = true
                binding.arrowAnimationView.setMargins(0, 0,
                    0, 0)
                binding.arrowAnimationView.rotation = 270F
                binding.arrowAnimationView.playAnimation()
            }
            else -> {
                binding.arrowAnimationView.isVisible = false
            }
        }
        binding.checkFaceTitle.text = when(nextMilestoneType) {
            GestureMilestoneType.UpHeadPitchMilestone -> getString(R.string.liveness_stage_face_up)
            GestureMilestoneType.DownHeadPitchMilestone -> getString(R.string.liveness_stage_face_down)
            GestureMilestoneType.OuterRightHeadYawMilestone -> getString(R.string.liveness_stage_face_right)
            GestureMilestoneType.OuterLeftHeadYawMilestone -> getString(R.string.liveness_stage_face_left)
            GestureMilestoneType.MouthOpenMilestone -> getString(R.string.liveness_stage_open_mouth)
            GestureMilestoneType.StraightHeadCheckMilestone -> getString(R.string.liveness_stage_check_face_pos)
        }
        blockProcessingByUI = false
    }

    fun navigateOnLivenessSessionEnd() {
        runOnUiThread {
            binding.arrowAnimationView.isVisible = false
            binding.faceAnimationView.isVisible = false
            binding.checkFaceTitle.text = getString(R.string.wait_for_liveness_start)
            vibrateDevice(this@VCheckLivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
            binding.imgViewStaticStageIndication.isVisible = true
            binding.stageSuccessAnimBorder.isVisible = true
            binding.livenessCosmeticsHolder.isVisible = false
            apiRequestTimer?.cancel()
            safeNavigateToResultDestination(R.id.action_dummyLivenessStartDestFragment_to_inProcessFragment)
        }
    }

    private fun safeNavigateToResultDestination(actionIdForNav: Int) {
        try {
            findNavController(R.id.liveness_host_fragment).navigate(actionIdForNav)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Attempt of nav to major obstacle was made, but was already on another fragment")
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Caught exception: Liveness Activity does not have a NavController set!")
        } catch (e: Exception) {
            showSingleToast(e.message)
        }
    }

    private fun onFatalObstacleWorthRetry(actionIdForNav: Int) {
        vibrateDevice(this@VCheckLivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
        finishLivenessSession()
        apiRequestTimer?.cancel()
        binding.livenessCosmeticsHolder.isVisible = false
        safeNavigateToResultDestination(actionIdForNav)
    }

    private fun animateStageSuccessFrame() {
        binding.stageSuccessAnimBorder.animate().alpha(1F).setDuration(
            BLOCK_PIPELINE_TIME_MILLIS / 2).setInterpolator(
            DecelerateInterpolator())
            .withEndAction {
                binding.stageSuccessAnimBorder.animate().alpha(0F).setDuration(
                    BLOCK_PIPELINE_TIME_MILLIS / 2)
                    .setInterpolator(AccelerateInterpolator()).start()
            }.start()
    }

    // -------------------------------------------- Lifecycle functions

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo: String = VCheckSDK.getSDKLangCode()
        val localeUpdatedContext: ContextWrapper =
            VCheckContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        apiRequestTimer?.cancel()
        takeImageTimer?.cancel()
        imageCapture = null
        imageCaptureExecutor.shutdown()
        limitedHCFBitmapList = null
        limitedHCFMuxer = null
        super.onDestroy()
    }

    private fun setHeader() {
        binding.logo.isVisible = VCheckSDK.showPartnerLogo

        if (VCheckSDK.showCloseSDKButton) {
            binding.closeSDKBtnHolder.isVisible = true
            binding.closeSDKBtnHolder.setOnClickListener {
                closeSDKFlow(false)
            }
        } else {
            binding.closeSDKBtnHolder.isVisible = false
        }
    }

    fun closeSDKFlow(shouldExecuteEndCallback: Boolean) {
        (VCheckDIContainer).mainRepository.setFirePartnerCallback(shouldExecuteEndCallback)
        (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        val intents = Intent(this@VCheckLivenessActivity, VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}