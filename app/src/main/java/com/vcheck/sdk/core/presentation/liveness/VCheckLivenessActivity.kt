package com.vcheck.sdk.core.presentation.liveness

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.video.*
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ActivityVcheckLivenessBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.LivenessGestureResponse
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.presentation.liveness.flow_logic.*
import com.vcheck.sdk.core.util.*
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer


@Suppress("DEPRECATION")
@OptIn(DelicateCoroutinesApi::class)
class VCheckLivenessActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LivenessActivity"
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 15000 //max is 15000 + tech delays
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 800 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 120 //may reduce a bit
        private const val DELAY_BEFORE_RECORDING_START_MILLIS: Long = 950
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
        private const val VIDEO_STREAM_WIDTH_LIMIT = 960
        private const val VIDEO_STREAM_HEIGHT_LIMIT = 720
    }

    lateinit var binding: ActivityVcheckLivenessBinding

    var mToast: Toast? = null

    private val scope = CoroutineScope(newSingleThreadContext("liveness"))

    private var milestoneFlow: StandardMilestoneFlow = StandardMilestoneFlow()
    var apiRequestTimer: Timer? = null
    var isLivenessSessionFinished: Boolean = false
    private var livenessSessionLimitCheckTime: Long = 0
    private var blockProcessingByUI: Boolean = false
    private var blockRequestByProcessing: Boolean = false

    private lateinit var cameraDevice: CameraDevice
    lateinit var mediaRecorder: MediaRecorder
    lateinit var previewSize: Size
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var isRecording = false

    private var gestureCheckBitmap: Bitmap? = null
    var videoPath: String? = null

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

        startBackgroundThread()

        setupFlowForNewLivenessSession()

        setMilestones()

        initSetupUI()

        indicateNextMilestone(milestoneFlow.getFirstStage(), true)

        setSurfaceTextureListener()
    }

    private fun setSurfaceTextureListener() {
        val textureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = getFrontFacingCameraId(cameraManager)
                val characteristics = cameraManager.getCameraCharacteristics(cameraId!!)
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val outputSizes = map!!.getOutputSizes(SurfaceTexture::class.java)

                previewSize = chooseOptimalSize(outputSizes, VIDEO_STREAM_WIDTH_LIMIT, VIDEO_STREAM_HEIGHT_LIMIT)
                Log.d(TAG, "STREAM OPTIMAL SIZE: ${previewSize.width}x${previewSize.height}")
                binding.cameraTextureView.setAspectRatio(previewSize.height, previewSize.width)

                openCamera()
            }
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                gestureCheckBitmap = binding.cameraTextureView.bitmap
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "onSurfaceTextureSizeChanged !")
            }
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }
        }
        binding.cameraTextureView.apply {
            surfaceTextureListener = textureListener
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startRecordingWhenReady()
            }
            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }
            override fun onError(camera: CameraDevice, error: Int) {
                onDisconnected(camera)
                Log.e(TAG, "openCamera onError() : code [$error]")
            }
        }
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = getFrontFacingCameraId(cameraManager)
            if (cameraId == null) {
                showSingleToast("No front camera detected!")
            } else {
                cameraManager.openCamera(cameraId, stateCallback, null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startRecordingWhenReady() {
        try {
            setUpMediaRecorder()
            val texture = binding.cameraTextureView.surfaceTexture?.apply {

                setDefaultBufferSize(previewSize.width, previewSize.height)
            }
            val surface = Surface(texture)
            val recordingRequestBuilder = cameraDevice.createCaptureRequest(
                CameraDevice.TEMPLATE_RECORD).apply {
                    addTarget(surface)
                    addTarget(mediaRecorder.surface)
            }
            recordingRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90)

            cameraDevice.createCaptureSession(listOf(surface, mediaRecorder.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {

                        session.setRepeatingRequest(recordingRequestBuilder.build(), null,
                            backgroundHandler)

                        Handler().postDelayed({
                            livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
                            setGestureRequestDebounceTimer()

                            isRecording = true
                            mediaRecorder.start()
                        }, DELAY_BEFORE_RECORDING_START_MILLIS)
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(this@VCheckLivenessActivity,
                            "Failed to start recording",
                            Toast.LENGTH_SHORT).show()
                    }
                }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            if (isRecording) {
                Log.d(TAG, "Stopping video recording..")
                mediaRecorder.stop()
                mediaRecorder.reset()
                mediaRecorder.release()
                isRecording = false
            }
        } catch (e: IllegalStateException) {
            Log.d(TAG, "mediaRecorder.stop() has thrown IllegalStateException; " +
                    "it's possible it was already been stopped.")
        }
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
        milestoneFlow.resetStages()
        isLivenessSessionFinished = false
    }

    private fun setGestureRequestDebounceTimer() {
        apiRequestTimer = fixedRateTimer("liveness_api_request_timer", false,
            0L, GESTURE_REQUEST_DEBOUNCE_MILLIS) {
            scope.launch {
                determineImageResult()
            }
        }
    }

    private fun finishLivenessSession() {
        apiRequestTimer?.cancel()
        isLivenessSessionFinished = true
        gestureCheckBitmap?.recycle()
        gestureCheckBitmap = null
        cameraDevice.close()
        stopBackgroundThread()
        scope.cancel()
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

                val rotatedBitmap = unMirrorBitmap(gestureCheckBitmap!!)

                val file = File(createTempFileForBitmapFrame(rotatedBitmap!!))

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
        Log.d(TAG, "GESTURE RESPONSE: ${it.success} | ${it.message} | ${it.errorCode}")
        blockRequestByProcessing = false
        runOnUiThread {
            if (!isLivenessSessionFinished) {
                if (milestoneFlow.areAllStagesPassed()) {
                    finishLivenessSession()
                    stopRecording()
                    navigateOnLivenessSessionEnd()
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

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("LivenessCameraBackground").also { it.start() }
        backgroundHandler = backgroundThread?.looper?.let { Handler(it) }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
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

    private fun navigateOnLivenessSessionEnd() {
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
        binding.livenessCosmeticsHolder.isVisible = false
        stopRecording()
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

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    override fun onStop() {
        stopRecording()
        super.onStop()
    }

    override fun onDestroy() {
        scope.cancel()
        apiRequestTimer?.cancel()
        gestureCheckBitmap?.recycle()
        cameraDevice.close()
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
        stopRecording()
        finishLivenessSession()
        (VCheckDIContainer).mainRepository.setFirePartnerCallback(shouldExecuteEndCallback)
        (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        val intents = Intent(this@VCheckLivenessActivity, VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}
