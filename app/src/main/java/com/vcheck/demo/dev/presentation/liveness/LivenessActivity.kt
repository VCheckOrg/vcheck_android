package com.vcheck.demo.dev.presentation.liveness

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.ActivityLivenessBinding
import com.vcheck.demo.dev.presentation.liveness.flow_logic.*
import com.vcheck.demo.dev.presentation.liveness.ui.CameraConnectionFragment
import com.vcheck.demo.dev.util.ContextUtils
import com.vcheck.demo.dev.util.setMargins
import com.vcheck.demo.dev.util.shouldDecreaseVideoStreamQuality
import com.vcheck.demo.dev.util.vibrateDevice
import com.vcheck.demo.dev.util.video.Muxer
import com.vcheck.demo.dev.util.video.MuxerConfig
import com.vcheck.demo.dev.util.video.MuxingCompletionListener
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList


class LivenessActivity : AppCompatActivity(),
    ImageReader.OnImageAvailableListener,
    MilestoneResultListener {

    companion object {
        const val TAG = "LivenessActivity"
        private const val RUN_PIPELINE_ON_GPU = false
        private const val STATIC_PIPELINE_IMAGE_MODE = true
        private const val REFINE_PIPELINE_LANDMARKS = false
        private const val MAX_MILESTONES_NUM = 468
        private const val DEBOUNCE_PROCESS_MILLIS = 50 //may reduce a bit
        private const val LIVENESS_TIME_LIMIT_MILLIS = 14000 //max is 15000
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 1200 //may reduce a bit
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
        private const val MAX_FRAMES_W_O_MAJOR_OBSTACLES = 12
        private const val MIN_FRAMES_FOR_MINOR_OBSTACLES = 4
        private const val MIN_AFFORDABLE_BRIGHTNESS_VALUE = 12.0 // on some old devices, values are really low!
    }

    private var binding: ActivityLivenessBinding? = null
    private var mToast: Toast? = null

    var streamSize: Size = Size(320, 240)
    private var bitmapArray: ArrayList<Bitmap>? = ArrayList()
    private var muxer: Muxer? = null
    var videoPath: String? = null //make private!

    var openLivenessCameraParams: LivenessCameraParams? = LivenessCameraParams()

    private var facemesh: FaceMesh? = null
    private var faceCheckDebounceTime: Long = 0
    private var livenessSessionLimitCheckTime: Long = 0
    private var isLivenessSessionFinished: Boolean = false
    private var blockProcessingByUI: Boolean = false

    private var multiFaceFrameCounter: Int = 0
    private var noFaceFrameCounter: Int = 0
    private var lowBrightnessFrameCounter: Int = 0
    private var minorObstacleFrameCounter: Int = 0

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private val brightnessListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lightQuantity = event.values[0]
            onBrightnessChanged(lightQuantity)
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} //Stub
    }

    private var milestoneFlow: StandardMilestoneFlow =
        StandardMilestoneFlow(this@LivenessActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLivenessBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        //determineAndSetStreamSize()

        setupBrightnessLevelListener()

        resetMilestonesForNewLivenessSession()

        setupStreamingModePipeline()

        setCameraFragment()

        initSetupUI()
    }

    private fun resetMilestonesForNewLivenessSession() {
        milestoneFlow = StandardMilestoneFlow(this@LivenessActivity)
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        faceCheckDebounceTime = SystemClock.elapsedRealtime()
        isLivenessSessionFinished = false
    }

    fun finishLivenessSession() {
        isLivenessSessionFinished = true
    }

    private fun setupStreamingModePipeline() {
        facemesh = FaceMesh(
            this@LivenessActivity,
            FaceMeshOptions.builder()
                .setStaticImageMode(STATIC_PIPELINE_IMAGE_MODE)
                .setRefineLandmarks(REFINE_PIPELINE_LANDMARKS)
                .setRunOnGpu(RUN_PIPELINE_ON_GPU)
                .setMaxNumFaces(2)
                .build())
        facemesh!!.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(TAG, "======= MediaPipe Face Mesh error : $message")
            FirebaseCrashlytics.getInstance().recordException(RuntimeException("MediaPipe Face Mesh error : $message"))
        }
        facemesh!!.setResultListener { faceMeshResult: FaceMeshResult ->
            // Before doing something that requires a lot of memory,
            // check to see whether the device is in a low memory state.
            val memoryInfo: ActivityManager.MemoryInfo = getAvailableMemory()
            if (!memoryInfo.lowMemory) {
                try {
                    if (!isLivenessSessionFinished && !blockProcessingByUI && enoughTimeForNextGesture()) {
                        processLandmarks(faceMeshResult)
                    } else {
                        if (!isLivenessSessionFinished && !blockProcessingByUI) {
                            runOnUiThread {
                                onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_noTimeFragment)
                            }
                        }
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException("Error in top-level" +
                            "MediaPipe setResultListener: ${e.message} | ${e.cause}"))
                }
            } else {
                showSingleToast("[TEST] LOW MEMORY; pausing face processing for a while")
                FirebaseCrashlytics.getInstance().recordException(Exception("Low memory caught!"))
            }
        }
    }

    override fun onObstacleMet(obstacleType: ObstacleType) {
        runOnUiThread {
            when (obstacleType) {
                ObstacleType.YAW_ANGLE -> {
                    minorObstacleFrameCounter += 1
                    if (minorObstacleFrameCounter > MIN_FRAMES_FOR_MINOR_OBSTACLES) {
                        binding!!.checkFaceTitle.setTextColor(resources.getColor(R.color.errorLight))
                        binding!!.checkFaceTitle.text = getString(R.string.line_face_obstacle)
                        delayedResetUIAfterObstacle()
                        minorObstacleFrameCounter = 0
                    }
                }
                ObstacleType.MULTIPLE_FACES_DETECTED -> {
                    onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_frameInterferenceFragment)
                }
                ObstacleType.NO_OR_PARTIAL_FACE_DETECTED -> {
                    onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_lookStraightErrorFragment)
                }
                ObstacleType.BRIGHTNESS_LEVEL_IS_LOW -> {
                    sensorManager?.unregisterListener(brightnessListener)
                    onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_tooDarkFragment)
                }
            }
        }
    }

    override fun onMilestoneResult(gestureMilestoneType: GestureMilestoneType) {
        blockProcessingByUI = true
        runOnUiThread {
            binding!!.faceAnimationView.isVisible = false
            binding!!.arrowAnimationView.isVisible = false
            when (gestureMilestoneType) {
                GestureMilestoneType.CheckHeadPositionMilestone -> {
                    setUIOnCheckHeadPositionMilestone()
                }
                GestureMilestoneType.OuterLeftHeadPitchMilestone -> {
                    setUIOnOuterLeftHeadPitchMilestone()
                }
                GestureMilestoneType.OuterRightHeadPitchMilestone -> {
                    setUIOnOuterRightHeadPitchMilestone()
                }
                GestureMilestoneType.MouthOpenMilestone -> {
                    delayedNavigateOnLivenessSessionEnd(true)
                }
                else -> {
                    //Stub. Cases in which results we are not straightly concerned
                }
            }
        }
    }

    private fun processLandmarks(faceMeshResult: FaceMeshResult) {
        // convert markers to 2DArray each 1 second or less (may vary)
        if (mayProcessNextLandmarkArray()) {
            //Log.d(TAG, "======== FACES: ${faceMeshResult.multiFaceLandmarks().size}")
            if (faceMeshResult.multiFaceLandmarks().size >= 2) {
                multiFaceFrameCounter += 1
                if (multiFaceFrameCounter >= MAX_FRAMES_W_O_MAJOR_OBSTACLES) {
                    multiFaceFrameCounter = 0
                    onObstacleMet(ObstacleType.MULTIPLE_FACES_DETECTED)
                }
            } else if (faceMeshResult.multiFaceLandmarks().isEmpty()) {
                noFaceFrameCounter += 1
                Log.d("LIVENESS", "-------- ___NO___ FACE DETECTED - FRAME COUNT: $noFaceFrameCounter")
                if (noFaceFrameCounter >= MAX_FRAMES_W_O_MAJOR_OBSTACLES) {
                    noFaceFrameCounter = 0
                    onObstacleMet(ObstacleType.NO_OR_PARTIAL_FACE_DETECTED)
                }
            } else {
                noFaceFrameCounter = 0
                multiFaceFrameCounter = 0
                val convertResult = get2DArrayFromMotionUpdate(faceMeshResult)
                if (convertResult != null) {
                    val faceAnglesCalcResultArr = LandmarksProcessingUtil.landmarksToEulerAngles(convertResult)
                    val pitchAngle = faceAnglesCalcResultArr[0]
                    val yawAngleAbs = kotlin.math.abs(faceAnglesCalcResultArr[1])
                    val mouthAspectRatio = LandmarksProcessingUtil.landmarksToMouthAspectRatio(convertResult)
                    //Log.d(TAG, "========= MOUTH ASPECT RATIO: $mouthAspectRatio | PITCH: $pitchAngle | YAW(abs) : $yawAngleAbs")
                    milestoneFlow.checkCurrentStage(pitchAngle, mouthAspectRatio, yawAngleAbs)
                }
                faceCheckDebounceTime = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun mayProcessNextLandmarkArray(): Boolean {
        return (SystemClock.elapsedRealtime() - faceCheckDebounceTime >= DEBOUNCE_PROCESS_MILLIS)
                && !isLivenessSessionFinished
    }

    private fun enoughTimeForNextGesture(): Boolean {
        return SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime <= LIVENESS_TIME_LIMIT_MILLIS
    }

    private fun getActualSessionTimeInSecs(): Double {
        return (SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime).toDouble() / 1000.0
    }

    private fun get2DArrayFromMotionUpdate(result: FaceMeshResult?) : D2Array<Double>? {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return null
        }
        val twoDimArray = mk.d2array(MAX_MILESTONES_NUM, 3) { it.toDouble() }

        result.multiFaceLandmarks()[0].landmarkList.forEachIndexed { idx, landmark ->
            val arr = mk.ndarray(doubleArrayOf(
                landmark.x.toDouble(),
                landmark.y.toDouble(),
                (1 - landmark.z).toDouble()))
            try {
                if (!arr.isEmpty()) {
                    twoDimArray[idx] = arr
                }
            } catch (e: IndexOutOfBoundsException) {
                //Stub; ignoring exception as matrix
                //may not contain all of MAX_MILESTONES_NUM in real-time
            }
        }
        return twoDimArray
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
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        val fragment: Fragment
        val camera2Fragment = CameraConnectionFragment.newInstance(
            object :
                CameraConnectionFragment.ConnectionCallback {
                override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                    openLivenessCameraParams?.previewHeight = size!!.height
                    openLivenessCameraParams?.previewWidth = size.width
                    openLivenessCameraParams?.sensorOrientation = cameraRotation - getScreenOrientation()
                }
            },
            this@LivenessActivity)

        camera2Fragment.setCamera(cameraId)
        fragment = camera2Fragment
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
                FirebaseCrashlytics.getInstance().recordException(error)
            }
        })

        val finalList = CopyOnWriteArrayList(bitmapArray!!)
        Thread {
            Log.d(TAG, "-------------------- MUXING......")
            muxer!!.mux(finalList)
        }.start()
    }

    private fun setUpMuxer() {

        //TODO (?) add logic for increasing framesPerImage / FPS based on one of factors:
        //        val finalSessionTime = getActualSessionTimeInSecs()
        //        val snapshotsSize = bitmapArray.size
        //TODO add little delay for MOUTH(last) video to capture in problematic cases!

        val framesPerImage = 1
        val framesPerSecond = 24F

        val muxerConfig = MuxerConfig(createVideoFile() ?: File.createTempFile(
            "faceVideo${System.currentTimeMillis()}", ".mp4",
                this@LivenessActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)),
            streamSize.height, streamSize.width, MediaFormat.MIMETYPE_VIDEO_AVC,
            framesPerImage, framesPerSecond, 2500000, iFrameInterval = 1) //3, 32F, 2500000, iFrameInterval = 50 (10))
        muxer = Muxer(this@LivenessActivity, muxerConfig)
    }

    fun processImage() {
        try {
            openLivenessCameraParams?.apply {

                imageConverter!!.run()
                rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
                rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)

                //sending bitmap to FaceMesh to process
                facemesh!!.send(rgbFrameBitmap!!)
                //bitmapArray.add(bitmap!!)
                bitmapArray?.add(rotateBitmap(rgbFrameBitmap!!)!!)
                //Log.d(TAG, "------------- PUT BITMAP TO ARRAY. SIZE: ${bitmapArray.size}")

                rgbFrameBitmap!!.recycle() //test!

                postInferenceCallback!!.run()
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            showSingleToast("[TEST]: ${e.message}")
        } catch (e: Error) {
            FirebaseCrashlytics.getInstance().recordException(e)
            showSingleToast("[TEST]: ${e.message}")
        }
    }

    private fun setupBrightnessLevelListener() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            // Success! Light sensor exists.
            lightSensor = (sensorManager as SensorManager).getDefaultSensor(Sensor.TYPE_LIGHT)
            sensorManager?.registerListener(
                brightnessListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d(TAG,"Light sensor is available")
        }
        else {
            // Failure! Light sensor not available.
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this@LivenessActivity,
                    "[Info] Light sensor not available for current device", Toast.LENGTH_LONG).show()
            },2000)
        }
    }

    private fun onBrightnessChanged(lightQuantity: Float) {
        Log.d("LIVENESS", "-------- BRIGHTNESS: $lightQuantity")
        if (lightQuantity < MIN_AFFORDABLE_BRIGHTNESS_VALUE && (lightQuantity > 5.0 || lightQuantity < 0.0)) {
            lowBrightnessFrameCounter += 1
            //Log.d("LIVENESS", "-------- LOW BRIGHTNESS - FRAME COUNT: $lowBrightnessFrameCounter")
            if (lowBrightnessFrameCounter >= MAX_FRAMES_W_O_MAJOR_OBSTACLES) {
                lowBrightnessFrameCounter = 0
                onObstacleMet(ObstacleType.BRIGHTNESS_LEVEL_IS_LOW)
            }
        } else {
            lowBrightnessFrameCounter = 0
        }
    }

    //@Throws(IOException::class)
    private fun createVideoFile(): File? {
        try {
            val storageDir: File =
                this@LivenessActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            return File.createTempFile(
                "faceVideo${System.currentTimeMillis()}", ".mp4", storageDir
            ).apply {
                videoPath = this.path
                Log.d("VIDEO", "SAVING A FILE: ${this.path}")
            }
        } catch (e: IOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            return null
        }
    }

    /// -------------------------------------------- UI functions

    private fun initSetupUI() {
        binding!!.stageSuccessAnimBorder.isVisible = false
        binding!!.checkFaceTitle.text = getString(R.string.wait_for_liveness_start)
        binding!!.imgViewStaticStageIndication.isVisible = false
        binding!!.arrowAnimationView.setMargins(null, null,
            300, null)
        binding!!.arrowAnimationView.rotation = 0F
        binding!!.arrowAnimationView.isVisible = false
    }

    private fun setUIOnCheckHeadPositionMilestone() {
        binding!!.imgViewStaticStageIndication.isVisible = false
        binding!!.arrowAnimationView.isVisible = true
        binding!!.faceAnimationView.isVisible = true
        binding!!.faceAnimationView.setAnimation(R.raw.left)
        binding!!.faceAnimationView.playAnimation()
        binding!!.arrowAnimationView.playAnimation()
        binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_face_left)
        blockProcessingByUI = false
    }

    private fun setUIOnOuterLeftHeadPitchMilestone() {
        vibrateDevice(this@LivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
        binding!!.imgViewStaticStageIndication.isVisible = true
        binding!!.stageSuccessAnimBorder.isVisible = true
        animateStageSuccessFrame()
        Handler(Looper.getMainLooper()).postDelayed ({
            binding!!.imgViewStaticStageIndication.isVisible = false
            binding!!.arrowAnimationView.isVisible = true
            binding!!.faceAnimationView.isVisible = true
            binding!!.faceAnimationView.cancelAnimation()
            binding!!.faceAnimationView.setAnimation(R.raw.right)
            binding!!.faceAnimationView.playAnimation()
            binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_face_right)
            binding!!.arrowAnimationView.rotation = 180F
            binding!!.arrowAnimationView.setMargins(null, null,
                -300, null)
            blockProcessingByUI = false
        }, BLOCK_PIPELINE_TIME_MILLIS)
    }

    private fun setUIOnOuterRightHeadPitchMilestone() {
        vibrateDevice(this@LivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
        binding!!.imgViewStaticStageIndication.isVisible = true
        binding!!.stageSuccessAnimBorder.isVisible = true
        animateStageSuccessFrame()
        Handler(Looper.getMainLooper()).postDelayed ({
            binding!!.imgViewStaticStageIndication.isVisible = false
            binding!!.arrowAnimationView.isVisible = false
            binding!!.faceAnimationView.isVisible = true
            binding!!.faceAnimationView.cancelAnimation()
            binding!!.faceAnimationView.setAnimation(R.raw.mouth)
            binding!!.faceAnimationView.playAnimation()
            binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_open_mouth)
            blockProcessingByUI = false
        }, BLOCK_PIPELINE_TIME_MILLIS)
    }

    private fun delayedNavigateOnLivenessSessionEnd(isVerificationSuccessful: Boolean) {
        binding!!.checkFaceTitle.text = getString(R.string.wait_for_liveness_start)
        if (isVerificationSuccessful) {
            vibrateDevice(this@LivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
            binding!!.imgViewStaticStageIndication.isVisible = true
            binding!!.stageSuccessAnimBorder.isVisible = true
        } else {
            binding!!.stageSuccessAnimBorder.isVisible = false
            binding!!.imgViewStaticStageIndication.isVisible = false
            binding!!.arrowAnimationView.cancelAnimation()
            binding!!.faceAnimationView.cancelAnimation()
            binding!!.arrowAnimationView.isVisible = false
            binding!!.faceAnimationView.isVisible = false
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding!!.livenessCosmeticsHolder.isVisible = false
            safeNavigateToResultDestination(R.id.action_dummyLivenessStartDestFragment_to_inProcessFragment)
        }, 1000)
    }

    private fun safeNavigateToResultDestination(actionIdForNav: Int) {
        try {
            findNavController(R.id.liveness_host_fragment).navigate(actionIdForNav)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Attempt of nav to major obstacle was made, but was already on another fragment")
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Caught exception: Liveness Activity does not have a NavController set!")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun delayedResetUIAfterObstacle() {
        Handler(Looper.getMainLooper()).postDelayed ({
            binding!!.checkFaceTitle.setTextColor(resources.getColor(R.color.white))
            when(milestoneFlow.getUndoneStage().milestoneType) {
                GestureMilestoneType.CheckHeadPositionMilestone -> {
                    binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_face_left)
                }
                GestureMilestoneType.OuterLeftHeadPitchMilestone -> {
                    binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_face_right)
                }
                GestureMilestoneType.OuterRightHeadPitchMilestone -> {
                    binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_open_mouth)
                }
                else -> {
                    // Stub
                }
            }
        }, 1200)
    }

    private fun onFatalObstacleWorthRetry(actionIdForNav: Int) {
        val actualAttemptsNum = (application as VcheckDemoApp).appContainer.mainRepository
            .getActualLivenessLocalAttempts(this@LivenessActivity)
        Log.d("LIVENESS", "========== ACTUAL ATTEMPTS NUM : $actualAttemptsNum")
        val maxAttemptsNum = (application as VcheckDemoApp).appContainer.mainRepository
            .getMaxLivenessLocalAttempts(this@LivenessActivity)
        if (actualAttemptsNum < maxAttemptsNum && !isLivenessSessionFinished) {
            (application as VcheckDemoApp).appContainer.mainRepository
                .incrementActualLivenessLocalAttempts(this@LivenessActivity)
            vibrateDevice(this@LivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
            finishLivenessSession()
            livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
            binding!!.livenessCosmeticsHolder.isVisible = false
            sensorManager?.unregisterListener(brightnessListener)
            safeNavigateToResultDestination(actionIdForNav)
        } else {
            delayedNavigateOnLivenessSessionEnd(false)
        }
    }

    private fun animateStageSuccessFrame() {
        binding!!.stageSuccessAnimBorder.animate().alpha(1F).setDuration(
            BLOCK_PIPELINE_TIME_MILLIS / 2).setInterpolator(
            DecelerateInterpolator())
            .withEndAction {
                binding!!.stageSuccessAnimBorder.animate().alpha(0F).setDuration(
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
        val localeToSwitchTo: String = ContextUtils.getSavedLanguage(newBase)
        Log.d("Ok", "======== attachBaseContext[LivenessActivity] LOCALE TO SWITCH TO : $localeToSwitchTo")
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    // Get a MemoryInfo object for the device's current memory status.
    private fun getAvailableMemory(): ActivityManager.MemoryInfo {
        val activityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager = null
        lightSensor = null
        facemesh?.close()
        bitmapArray = null
        muxer = null
        openLivenessCameraParams = null
    }

    // !
    //    private fun determineAndSetStreamSize() {
    //        streamSize = if (shouldDecreaseVideoStreamQuality()) {
    //            Size(320, 240)
    //        } else {
    //            Size(960, 720)
    //        }
    //        showSingleToast("[TEST] setting resolution to : ${streamSize.width}x${streamSize.height}")
    //    }

    //            Log.i(TAG, "--------------- IDX: $idx")
    //            Log.i(TAG, "--------------- x: ${arr[0]} | y: ${arr[1]} | z: ${arr[2]}")
    //Log.d(TAG, "=========== EULER ANGLES " +
    //" | pitch: ${eulerAnglesResultArr[0]}")  // from -30.0 to 30.0 degrees
    //" | yaw: ${eulerAnglesResultArr[1]}" +
    //" | roll: ${eulerAnglesResultArr[2]}")

    //        object : CountDownTimer(15000, 1000) {
    //            override fun onTick(duration: Long) {
    //            }
    //
    //            override fun onFinish() {
    //                Log.d(TAG,  "======================================================== FINISH")
    //            }
    //        }.start()

    //    fun resetUIForNewLivenessSession() {
    //        binding!!.stageSuccessAnimBorder.isVisible = false
    //        binding!!.livenessCosmeticsHolder.isVisible = true
    //        binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_check_face_pos)
    //        binding!!.faceAnimationView.cancelAnimation()
    //        binding!!.arrowAnimationView.cancelAnimation()
    //        binding!!.arrowAnimationView.rotation = 0F
    //        binding!!.arrowAnimationView.setMargins(300, null,
    //            null, null)
    //        binding!!.arrowAnimationView.isVisible = false
    //    }

//    private fun isFacePartiallyVisible(faceMeshResult: FaceMeshResult): Boolean {
//        //1, 17, 159, 386
//        //TODO test!
//        Log.d("LIVENESS", "LEFT EYE VIS: x - ${result.multiFaceLandmarks()[0].landmarkList[52].hasX()}" +
//                " y - ${result.multiFaceLandmarks()[0].landmarkList[52].hasY()}" +
//                " z - ${result.multiFaceLandmarks()[0].landmarkList[52].hasZ()}")
//        Log.d("LIVENESS", "RIGHT EYE VIS: x - ${result.multiFaceLandmarks()[0].landmarkList[282].hasX()}" +
//                " y - ${result.multiFaceLandmarks()[0].landmarkList[282].hasY()}" +
//                " z - ${result.multiFaceLandmarks()[0].landmarkList[282].hasZ()}")
//        Log.d("LIVENESS", "MOUTH VIS: x - ${result.multiFaceLandmarks()[0].landmarkList[17].hasX()}" +
//                " y - ${result.multiFaceLandmarks()[0].landmarkList[17].hasY()}" +
//                " z - ${result.multiFaceLandmarks()[0].landmarkList[17].hasZ()}")
//        Log.d("LIVENESS", "LEFT EYE VIS: ${faceMeshResult.multiFaceLandmarks()[0].landmarkList[52].visibility}")
//        Log.d("LIVENESS", "RIGHT EYE VIS: ${faceMeshResult.multiFaceLandmarks()[0].landmarkList[282].visibility}")
//        Log.d("LIVENESS", "MOUTH VIS: ${faceMeshResult.multiFaceLandmarks()[0].landmarkList[17].visibility}")
//        return !faceMeshResult.multiFaceLandmarks()[0].landmarkList[52].isInitialized
//                || !faceMeshResult.multiFaceLandmarks()[0].landmarkList[282].isInitialized
//                || !faceMeshResult.multiFaceLandmarks()[0].landmarkList[17].isInitialized
//    }

}
