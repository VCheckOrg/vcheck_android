package com.vcheck.sdk.core.presentation.liveness

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.media.MediaFormat
import android.os.*
import android.util.Log
import android.util.Size
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.ActivityVcheckLivenessBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.LivenessGestureResponse
import com.vcheck.sdk.core.domain.SegmentationGestureResponse
import com.vcheck.sdk.core.presentation.VCheckStartupActivity
import com.vcheck.sdk.core.presentation.liveness.flow_logic.*
import com.vcheck.sdk.core.presentation.liveness.ui.LivenessCameraConnectionFragment
import com.vcheck.sdk.core.presentation.segmentation.VCheckSegmentationActivity
import com.vcheck.sdk.core.util.VCheckContextUtils
import com.vcheck.sdk.core.util.setMargins
import com.vcheck.sdk.core.util.sizeInKb
import com.vcheck.sdk.core.util.vibrateDevice
import com.vcheck.sdk.core.util.video.Muxer
import com.vcheck.sdk.core.util.video.MuxerConfig
import com.vcheck.sdk.core.util.video.MuxingCompletionListener
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.fixedRateTimer

@OptIn(DelicateCoroutinesApi::class)
class VCheckLivenessActivity : AppCompatActivity(),
    ImageReader.OnImageAvailableListener {

    companion object {
        const val TAG = "LivenessActivity"
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 15000 //max is 15000
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 800 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 250
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
    }

    private val scope = CoroutineScope(newSingleThreadContext("liveness"))

    private lateinit var binding: ActivityVcheckLivenessBinding
    private var mToast: Toast? = null

    var streamSize: Size = Size(640, 480)
    private var bitmapList: ArrayList<Bitmap>? = ArrayList()
    private var muxer: Muxer? = null
    var videoPath: String? = null //make private!
    var openLivenessCameraParams: LivenessCameraParams? = LivenessCameraParams()

    private var camera2Fragment: LivenessCameraConnectionFragment? = null

    private var livenessSessionLimitCheckTime: Long = 0
    private var isLivenessSessionFinished: Boolean = false
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

        resetFlowForNewLivenessSession()

        setMilestones()

        setCameraFragment()

        initSetupUI()

        indicateNextMilestone(milestoneFlow.getFirstStage(), true)
    }

    private fun setMilestones() {
        val milestonesList = VCheckDIContainer.mainRepository.getLivenessMilestonesList()
        if (milestonesList != null) {
            milestoneFlow.setStagesList(milestonesList)
        } else {
            showSingleToast("Dynamic milestone list not found: probably, milestone list was not " +
                    "retrieved form verification service or not cached properly.")
        }
    }

    private fun resetFlowForNewLivenessSession() {
        milestoneFlow.resetStages()
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        isLivenessSessionFinished = false
        setGestureRequestDebounceTimer()
    }

    private fun setGestureRequestDebounceTimer() {
        fixedRateTimer("timer", false, 0L, GESTURE_REQUEST_DEBOUNCE_MILLIS) {
            scope.launch {
                determineImageResult()
            }
        }
    }

    fun finishLivenessSession() {
        isLivenessSessionFinished = true
        gestureCheckBitmap = null
    }

    private fun enoughTimeForNextGesture(): Boolean {
        return SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime <= LIVENESS_TIME_LIMIT_MILLIS
    }

    private fun setCameraFragment() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraId: String? = null
        try {
            for (cameraIdx in cameraManager.cameraIdList) {
                val chars: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdx)
                if (CameraCharacteristics.LENS_FACING_FRONT == chars.get(CameraCharacteristics.LENS_FACING)) {
                    cameraId = cameraIdx
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FRONT CAMERA DETECTION ERROR: ${e.message}")
            showSingleToast(e.message)
        }

        camera2Fragment = LivenessCameraConnectionFragment.newInstance(
            object : LivenessCameraConnectionFragment.ConnectionCallback {
                override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                    openLivenessCameraParams?.previewHeight = size!!.height
                    openLivenessCameraParams?.previewWidth = size.width
                    openLivenessCameraParams?.sensorOrientation = cameraRotation - getScreenOrientation()
                }
            },
            this@VCheckLivenessActivity)

        camera2Fragment!!.setCamera(cameraId)

        val fragment: Fragment = camera2Fragment!!
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun onImageAvailable(reader: ImageReader?) {
        //calling verbose extension function, which leads to processImage()
        if (!isLivenessSessionFinished) {
            onImageAvailableImpl(reader)
        }
    }

    fun processVideoOnResult(videoProcessingListener: VideoProcessingListener) {
        setUpMuxer()

        muxer!!.setOnMuxingCompletedListener(object : MuxingCompletionListener {
            override fun onVideoSuccessful(file: File) {
                Log.d(TAG, "Video muxed - file path: ${file.absolutePath}")
                runOnUiThread {
                    videoProcessingListener.onVideoProcessed(file.path)
                }
            }
            override fun onVideoError(error: Throwable) {
                Log.e(TAG, "There was an error muxing the video")
                showSingleToast(error.message)
            }
        })

        val finalList = CopyOnWriteArrayList(bitmapList!!)
        Thread { muxer!!.mux(finalList) }.start()
    }

    private fun setUpMuxer() {
        val framesPerImage = 1
        val framesPerSecond = 24F
        val bitrate = 2500000
        val muxerConfig = MuxerConfig(createVideoFile() ?: File.createTempFile(
            "faceVideo${System.currentTimeMillis()}", ".mp4",
                this@VCheckLivenessActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)),
            streamSize.height, streamSize.width, MediaFormat.MIMETYPE_VIDEO_AVC,
            framesPerImage, framesPerSecond, bitrate, iFrameInterval = 1) //3, 32F, 2500000, iFrameInterval = 50 (10))
        muxer = Muxer(this@VCheckLivenessActivity, muxerConfig)
    }

    fun processImage() {
        try {
            Handler(Looper.getMainLooper()).post {
                openLivenessCameraParams?.apply {

                    imageConverter!!.run()
                    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
                    rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)

                    val finalBitmap = rotateBitmap(rgbFrameBitmap!!)!!
                    //caching bitmap to array/list:
                    bitmapList?.add(finalBitmap)
                    //recycling bitmap:
                    rgbFrameBitmap!!.recycle()
                    //running post-inference callback
                    postInferenceCallback!!.run()
                    //updating cached bitmap for gesture request
                    gestureCheckBitmap = finalBitmap
                }
            }
        } catch (e: Exception) {
            showSingleToast(e.message)
            showSingleToast("[TEST-processImage ex]: ${e.message}")
        } catch (e: Error) {
            showSingleToast(e.message)
            showSingleToast("[TEST-processImage err]: ${e.message}")
        }
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
                    val compressedImageFile = Compressor.compress(this@VCheckLivenessActivity, file) {
                        destination(file)
                        size(99_000) // ~96 KB
                    }
                    MultipartBody.Part.createFormData(
                        "image.jpg", compressedImageFile.name, compressedImageFile.asRequestBody("image/jpeg".toMediaType()))
                } catch (e: Exception) {
                    Log.d(TAG, "Exception while compressing Liveness frame image. Attempting to send default frame. \n${e.printStackTrace()}")
                    MultipartBody.Part.createFormData(
                        "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                } catch (e: Error) {
                    Log.d(TAG, "Error while compressing Liveness frame image. Attempting to send default frame. \n${e.printStackTrace()}")
                    MultipartBody.Part.createFormData(
                        "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                }

                val currentGesture = milestoneFlow.getGestureRequestFromCurrentStage()
                val response = VCheckDIContainer.mainRepository.sendLivenessGestureAttempt(
                        image, MultipartBody.Part.createFormData("gesture", currentGesture))

                if (response != null) {
                    processCheckResult(response)
                } else {
                    Log.d(VCheckSegmentationActivity.TAG, "========= --- RESPONSE FOR IDX IS NULL!")
                }
            }
        } else {
            //if (!isLivenessSessionFinished && !blockProcessingByUI) { //obsolete logic (?)
            if (!enoughTimeForNextGesture()) {
                runOnUiThread {
                    onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_noTimeFragment)
                }
            }
        }
    }

    private fun processCheckResult(it: LivenessGestureResponse) {
        blockRequestByProcessing = false
        runOnUiThread {
            if (!isLivenessSessionFinished) {
                if (milestoneFlow.areAllStagesPassed()) {
                    finishLivenessSession()
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

    private fun createVideoFile(): File? {
        return try {
            val storageDir: File =
                this@VCheckLivenessActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            File.createTempFile(
                "faceVideo${System.currentTimeMillis()}", ".mp4", storageDir).apply {
                videoPath = this.path
                Log.d("VIDEO", "SAVING A FILE: ${this.path}")
            }
        } catch (e: IOException) {
            showSingleToast(e.message)
            null
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
            else -> null
        }
        if (faceAnimeRes != null) {
            binding.faceAnimationView.isVisible = true
            binding.faceAnimationView.setAnimation(faceAnimeRes)
            binding.faceAnimationView.playAnimation()
        } else {
            binding.faceAnimationView.isVisible = true
            binding.faceAnimationView.setAnimation(R.raw.mouth)
            binding.faceAnimationView.pauseAnimation()
        }

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
            camera2Fragment?.onPause() //!
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
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
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

    private fun showSingleToast(message: String?) {
        if (mToast != null) {
            mToast?.cancel()
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        mToast?.show()
    }

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
        bitmapList = null
        muxer = null
        openLivenessCameraParams = null

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