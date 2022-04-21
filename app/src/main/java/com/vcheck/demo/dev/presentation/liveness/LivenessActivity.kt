package com.vcheck.demo.dev.presentation.liveness

import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.ActivityLivenessBinding
import com.vcheck.demo.dev.presentation.liveness.flow_logic.GestureMilestoneType
import com.vcheck.demo.dev.presentation.liveness.flow_logic.MilestoneResultListener
import com.vcheck.demo.dev.presentation.liveness.flow_logic.StandardMilestoneFlow
import com.vcheck.demo.dev.presentation.liveness.flow_logic.LandmarksProcessingUtil
import com.vcheck.demo.dev.presentation.liveness.flow_logic.LivenessCameraParams
import com.vcheck.demo.dev.presentation.liveness.flow_logic.getScreenOrientation
import com.vcheck.demo.dev.presentation.liveness.flow_logic.onImageAvailableImpl
import com.vcheck.demo.dev.presentation.liveness.ui.CameraConnectionFragment
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set

class LivenessActivity : AppCompatActivity(),
    ImageReader.OnImageAvailableListener,
    MilestoneResultListener {

    companion object {
        const val TAG = "LivenessActivity"
        private const val RUN_PIPELINE_ON_GPU = true
        private const val STATIC_PIPELINE_IMAGE_MODE = true
        private const val REFINE_PIPELINE_LANDMARKS = true
        private const val DEBOUNCE_PROCESS_MILLIS = 600
    }

    //refactor to protected
    val openLivenessCameraParams: LivenessCameraParams = LivenessCameraParams()

    private var facemesh: FaceMesh? = null
    private var faceCheckDebounceTime: Long = 0

    private var binding: ActivityLivenessBinding? = null

    private val milestoneFlow: StandardMilestoneFlow =
        StandardMilestoneFlow(this@LivenessActivity)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_liveness)

        binding = ActivityLivenessBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        faceCheckDebounceTime = SystemClock.elapsedRealtime()
        setupStreamingModePipeline()

        setCameraFragment()

        binding!!.faceAnimationView.repeatCount = LottieDrawable.INFINITE
        binding!!.faceAnimationView.playAnimation()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupStreamingModePipeline() {
        facemesh = FaceMesh(
            this@LivenessActivity,
            FaceMeshOptions.builder()
                .setStaticImageMode(STATIC_PIPELINE_IMAGE_MODE)
                .setRefineLandmarks(REFINE_PIPELINE_LANDMARKS)
                .setRunOnGpu(RUN_PIPELINE_ON_GPU)
                .build())
        facemesh!!.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(TAG, "MediaPipe Face Mesh error : $message")
        }
        facemesh!!.setResultListener { faceMeshResult: FaceMeshResult ->
            processLandmarks(faceMeshResult)
        }
    }

    override fun onMilestoneResult(gestureMilestoneType: GestureMilestoneType) {
        when (gestureMilestoneType) {
            GestureMilestoneType.CheckHeadPositionMilestone -> {
                Log.d(TAG, "-----------======== INIT STAGE (0)")
            }
            GestureMilestoneType.OuterLeftHeadPitchMilestone -> {
                Log.d(TAG, "-----------======== HEAD LEFT STAGE (1)")
            }
            GestureMilestoneType.OuterRightHeadPitchMilestone -> {
                Log.d(TAG, "-----------======== HEAD RIGHT STAGE (2)")
            }
            GestureMilestoneType.MouthOpenMilestone -> {
                Log.d(TAG, "-----------======== MOUTH OPENED STAGE (3)")
            } else -> {
                //Cases in which we are not concerned
            }
        }
    }

    private fun processLandmarks(faceMeshResult: FaceMeshResult) {
        // convert markers to 2DArray each 1 second or less (may vary)
        if (SystemClock.elapsedRealtime() - faceCheckDebounceTime >= DEBOUNCE_PROCESS_MILLIS) {
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

    private fun get2DArrayFromMotionUpdate(result: FaceMeshResult?) : D2Array<Double>? {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return null
        }
        val twoDimArray = mk.d2array(468, 3) { it.toDouble() }

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
                //Log.w(TAG, e.message ?: "2D MARKER ARRAY: caught IndexOutOfBoundsException")
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
                //val keys: List<CameraCharacteristics.Key<*>> = chars.keys
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
            R.layout.camera_fragment,
            Size(640, 480)
        )
        camera2Fragment.setCamera(cameraId)
        fragment = camera2Fragment
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    //TODO getting frames of live camera footage and passing them to model
    override fun onImageAvailable(reader: ImageReader?) {
        onImageAvailableImpl(reader)
    }

    fun processImage() {
        openLivenessCameraParams.apply {
            imageConverter!!.run()
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
            rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)

            //sending bitmap to FaceMesh to process
            facemesh!!.send(rgbFrameBitmap)

            postInferenceCallback!!.run()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //            Log.i(TAG, "--------------- IDX: $idx")
    //            Log.i(TAG, "--------------- x: ${arr[0]} | y: ${arr[1]} | z: ${arr[2]}")

    //Log.d(TAG, "=========== EULER ANGLES " +
    //" | pitch: ${eulerAnglesResultArr[0]}")  // from -30.0 to 30.0 degrees
    //" | yaw: ${eulerAnglesResultArr[1]}" +
    //" | roll: ${eulerAnglesResultArr[2]}")
    //Log.d(TAG, "========= MOUTH ASPECT RATIO: $mouthAspectRatio")  // >= 055
}