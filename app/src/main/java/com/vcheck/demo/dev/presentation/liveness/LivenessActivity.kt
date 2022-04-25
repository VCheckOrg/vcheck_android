package com.vcheck.demo.dev.presentation.liveness

import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.media.MediaFormat
import android.os.*
import android.util.Log
import android.util.Size
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.ActivityLivenessBinding
import com.vcheck.demo.dev.presentation.liveness.flow_logic.*
import com.vcheck.demo.dev.presentation.liveness.ui.CameraConnectionFragment
import com.vcheck.demo.dev.util.setMargins
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
import java.lang.IllegalArgumentException
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
        private const val DEBOUNCE_PROCESS_MILLIS = 400 //may reduce a bit
        private const val LIVENESS_TIME_LIMIT_MILLIS = 15000
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 1400 //may reduce a bit
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
    }

    var bitmapArray: ArrayList<Bitmap> = ArrayList()
    private lateinit var muxer: Muxer
    var videoPath: String? = null

    //refactor to protected
    val openLivenessCameraParams: LivenessCameraParams = LivenessCameraParams()

    private var facemesh: FaceMesh? = null
    private var faceCheckDebounceTime: Long = 0
    private var livenessSessionLimitCheckTime: Long = 0
    private var isLivenessSessionFinished: Boolean = false
    private var blockProcessingByUI: Boolean = false

    private var binding: ActivityLivenessBinding? = null

    private var milestoneFlow: StandardMilestoneFlow =
        StandardMilestoneFlow(this@LivenessActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLivenessBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setUpMuxer()

        resetMilestonesForNewLivenessSession()

        setupStreamingModePipeline()

        setCameraFragment()

        initSetupUI()
    }

    fun resetMilestonesForNewLivenessSession() {
        milestoneFlow = StandardMilestoneFlow(this@LivenessActivity)
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        faceCheckDebounceTime = SystemClock.elapsedRealtime()
        isLivenessSessionFinished = false
    }

    fun finishLivenessSession() {
        isLivenessSessionFinished = true
    }

    private fun setUpMuxer() {
        val muxerConfig = MuxerConfig(createVideoFile(),
            720, 960, MediaFormat.MIMETYPE_VIDEO_AVC,
            3, 32F, 2500000, iFrameInterval = 50)
        //TODO check number of output bitmaps in list on different devices after 15 seconds!
        muxer = Muxer(this@LivenessActivity, muxerConfig)
    }

    private fun setupStreamingModePipeline() {
        facemesh = FaceMesh(
            this@LivenessActivity,
            FaceMeshOptions.builder()
                .setStaticImageMode(STATIC_PIPELINE_IMAGE_MODE)
                .setRefineLandmarks(REFINE_PIPELINE_LANDMARKS)
                .setRunOnGpu(RUN_PIPELINE_ON_GPU)
                .setMaxNumFaces(1)
                .build())
        facemesh!!.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(TAG, "======= MediaPipe Face Mesh error : $message")
        }
        facemesh!!.setResultListener { faceMeshResult: FaceMeshResult ->
            if (!isLivenessSessionFinished && !blockProcessingByUI && enoughTimeForNextGesture()) {
                processLandmarks(faceMeshResult)
            } else {
                if (!isLivenessSessionFinished && !blockProcessingByUI) {
                    runOnUiThread {
                        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
                        binding!!.livenessCosmeticsHolder.isVisible = false
                        findNavController(R.id.liveness_host_fragment)
                            .navigate(R.id.action_dummyLivenessStartDestFragment_to_noTimeFragment)
                    }
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
                    binding!!.livenessCosmeticsHolder.isVisible = false
                    vibrateDevice(this@LivenessActivity, STAGE_VIBRATION_DURATION_MILLIS)
                    try {
                        findNavController(R.id.liveness_host_fragment)
                            .navigate(R.id.action_dummyLivenessStartDestFragment_to_inProcessFragment)
                    } catch (e: IllegalArgumentException) {
                        Log.d(TAG, "Attempt to nev to success was made, but was already on another fragment")
                    }
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
            val convertResult = get2DArrayFromMotionUpdate(faceMeshResult)
            if (convertResult != null) {
                val pitchAngle = LandmarksProcessingUtil.landmarksToEulerAngles(convertResult)[0]
                val mouthAspectRatio = LandmarksProcessingUtil.landmarksToMouthAspectRatio(convertResult)

                Log.d(TAG, "========= MOUTH ASPECT RATIO: $mouthAspectRatio | PITCH: $pitchAngle")
                milestoneFlow.checkCurrentStage(pitchAngle, mouthAspectRatio)
            }
            faceCheckDebounceTime = SystemClock.elapsedRealtime()
        }
    }

    private fun mayProcessNextLandmarkArray(): Boolean {
        return (SystemClock.elapsedRealtime() - faceCheckDebounceTime >= DEBOUNCE_PROCESS_MILLIS)
                && !isLivenessSessionFinished
    }

    private fun enoughTimeForNextGesture(): Boolean {
        return SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime <= LIVENESS_TIME_LIMIT_MILLIS
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
                (1 - landmark.z).toDouble()))  //was Float typing!
            try {
                if (!arr.isEmpty()) {
                    twoDimArray[idx] = arr
                }
            } catch (e: IndexOutOfBoundsException) {
                //Stub; ignoring exception as at real-time matrix
                //may not contain all of MAX_MILESTONES_NUM
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
        }

        val fragment: Fragment
        val camera2Fragment = CameraConnectionFragment.newInstance(
            object :
                CameraConnectionFragment.ConnectionCallback {
                override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                    openLivenessCameraParams.previewHeight = size!!.height
                    openLivenessCameraParams.previewWidth = size.width
                    openLivenessCameraParams.sensorOrientation = cameraRotation - getScreenOrientation()
                }
            },
            this,
            R.layout.camera_fragment)
            //Size(960, 720) //640x480

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
        muxer.setOnMuxingCompletedListener(object : MuxingCompletionListener {
            override fun onVideoSuccessful(file: File) {
                Log.d(TAG, "Video muxed - file path: ${file.absolutePath}")
                runOnUiThread {
                    videoProcessingListener.onVideoProcessed(file.path)
                }
            }
            override fun onVideoError(error: Throwable) {
                Log.e(TAG, "There was an error muxing the video")
            }
        })

        val finalList = CopyOnWriteArrayList(bitmapArray)
        Thread {
            Log.d(TAG, "-------------------- MUXING......")
            muxer.mux(finalList)
        }.start()
    }

    fun processImage() {
        openLivenessCameraParams.apply {
            imageConverter!!.run()
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
            rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)

            val bitmap = rgbFrameBitmap
            //sending bitmap to FaceMesh to process

            facemesh!!.send(bitmap)
            //bitmapArray.add(bitmap!!)

            bitmapArray.add(rotateBitmap(bitmap!!)!!)
            //Log.d(TAG, "------------- PUT BITMAP TO ARRAY. SIZE: ${bitmapArray.size}")

            postInferenceCallback!!.run()
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

    fun resetUIForNewLivenessSession() {
        binding!!.stageSuccessAnimBorder.isVisible = false
        binding!!.livenessCosmeticsHolder.isVisible = true
        binding!!.checkFaceTitle.text = getString(R.string.liveness_stage_check_face_pos)
        binding!!.faceAnimationView.cancelAnimation()
        binding!!.arrowAnimationView.cancelAnimation()
        binding!!.arrowAnimationView.rotation = 0F
        binding!!.arrowAnimationView.setMargins(300, null,
            null, null)
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

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        val storageDir: File =
            this@LivenessActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "faceVideo${System.currentTimeMillis()}", ".mp4", storageDir
        ).apply {
            videoPath = this.path
            Log.d("VIDEO", "SAVING A FILE: ${this.path}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //TODO
    }

    //            Log.i(TAG, "--------------- IDX: $idx")
    //            Log.i(TAG, "--------------- x: ${arr[0]} | y: ${arr[1]} | z: ${arr[2]}")
    //Log.d(TAG, "=========== EULER ANGLES " +
    //" | pitch: ${eulerAnglesResultArr[0]}")  // from -30.0 to 30.0 degrees
    //" | yaw: ${eulerAnglesResultArr[1]}" +
    //" | roll: ${eulerAnglesResultArr[2]}")
}
