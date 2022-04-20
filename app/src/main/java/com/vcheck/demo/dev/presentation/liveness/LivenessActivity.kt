package com.vcheck.demo.dev.presentation.liveness

import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.util.ImageUtils
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import java.lang.IndexOutOfBoundsException
import java.lang.RuntimeException


class LivenessActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener {

    companion object {
        const val TAG = "LivenessActivity"
        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
        private const val DEBOUNCE_PROCESS_MILLIS = 600
    }

    private var facemesh: FaceMesh? = null
    private var cameraInput: CameraInput? = null
    private var glSurfaceView: SolutionGlSurfaceView<FaceMeshResult>? = null

    private var debounceTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liveness)

        debounceTime = SystemClock.elapsedRealtime()
        setupStreamingModePipeline()

        setFragment()
    }

    var previewHeight = 0;
    var previewWidth = 0
    var sensorOrientation = 0;

    fun setupStreamingModePipeline() {
        facemesh = FaceMesh(
            this@LivenessActivity,
            FaceMeshOptions.builder()
                .setStaticImageMode(true)
                .setRefineLandmarks(true)
                .setRunOnGpu(RUN_ON_GPU)
                .build()
        )
        facemesh!!.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(TAG, "MediaPipe Face Mesh error:$message")
        }

        facemesh!!.setResultListener { faceMeshResult: FaceMeshResult ->
            //logNoseLandmark(faceMeshResult,  /*showPixelValues=*/false)

            //Log.d(TAG, "----  RESULT LISTENER WORKED")
            processLandmarks(faceMeshResult)

//            glSurfaceView!!.setRenderData(faceMeshResult)
//            glSurfaceView!!.requestRender()
        }
    }

    private fun processLandmarks(faceMeshResult: FaceMeshResult) {
        // convert markers to 2DArray each 1 second (may vary)
        if (SystemClock.elapsedRealtime() - debounceTime >= DEBOUNCE_PROCESS_MILLIS) {
            Log.d(TAG, "----  RESULT LISTENER WORKED WITH DEBOUNCE")
            val convertResult = get2DArrayFromMotionUpdate(faceMeshResult)
            if (convertResult != null) {
                val eulerAnglesResultArr = LandmarkUtil.landmarksToEulerAngles(convertResult)
//                Log.d(TAG, "=========== EULER ANGLES " +
//                            " | pitch: ${eulerAnglesResultArr[0]}")  // from -30.0 to 30.0 degrees
                //" | yaw: ${eulerAnglesResultArr[1]}" +
                //" | roll: ${eulerAnglesResultArr[2]}")

                val mouthAspectRatio = LandmarkUtil.landmarksToMouthAspectRatio(convertResult)
                Log.d(TAG, "========= MOUTH ASPECT RATIO: $mouthAspectRatio")  // >= 055
            }
            debounceTime = SystemClock.elapsedRealtime()
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

//            Log.i(TAG, "--------------- IDX: $idx")
//            Log.i(TAG, "--------------- x: ${arr[0]} | y: ${arr[1]} | z: ${arr[2]}")

            try {
                if (!arr.isEmpty()) {
                    twoDimArray[idx] = arr
                }
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e.message ?: "2D MARKER ARRAY: caught IndexOutOfBoundsException")
            }
        }
        return twoDimArray
    }

    //TODO fragment which show llive footage from camera
    protected fun setFragment() {
        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraId: String? = null
//        try {
//            cameraId = manager.cameraIdList[1]
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
        try {
            for (cameraIdx in manager.cameraIdList) {
                val chars: CameraCharacteristics = manager.getCameraCharacteristics(cameraIdx)
                //val keys: List<CameraCharacteristics.Key<*>> = chars.keys
                if (CameraCharacteristics.LENS_FACING_FRONT == chars.get(CameraCharacteristics.LENS_FACING)) {
                    // This is the one we want.
                    cameraId = cameraIdx
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "CAMERA ID ERROR: ${e.message}")
        }
        val fragment: Fragment
        val camera2Fragment = CameraConnectionFragment.newInstance(
            object :
                CameraConnectionFragment.ConnectionCallback {
                override fun onPreviewSizeChosen(size: Size?, rotation: Int) {
                    previewHeight = size!!.height
                    previewWidth = size.width
                    sensorOrientation = rotation - getScreenOrientation()
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

    protected fun getScreenOrientation(): Int {
        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }

    //TODO getting frames of live camera footage and passing them to model
    private var isProcessingFrame = false
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var rgbBytes: IntArray? = null
    private var yRowStride = 0
    private var postInferenceCallback: Runnable? = null
    private var imageConverter: Runnable? = null
    private var rgbFrameBitmap: Bitmap? = null
    override fun onImageAvailable(reader: ImageReader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return
        }
        if (rgbBytes == null) {
            rgbBytes = IntArray(previewWidth * previewHeight)
        }
        try {
            val image = reader.acquireLatestImage() ?: return
            if (isProcessingFrame) {
                image.close()
                return
            }
            isProcessingFrame = true
            val planes = image.planes
            fillBytes(planes, yuvBytes)
            yRowStride = planes[0].rowStride
            val uvRowStride = planes[1].rowStride
            val uvPixelStride = planes[1].pixelStride
            imageConverter = Runnable {
                ImageUtils.convertYUV420ToARGB8888(
                    yuvBytes[0]!!,
                    yuvBytes[1]!!,
                    yuvBytes[2]!!,
                    previewWidth,
                    previewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    rgbBytes!!
                )
            }
            postInferenceCallback = Runnable {
                image.close()
                isProcessingFrame = false
            }
            processImage()
        } catch (e: Exception) {
            return
        }
    }


    private fun processImage() {
        imageConverter!!.run()
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)


        facemesh!!.send(rgbFrameBitmap)

        postInferenceCallback!!.run()
    }

    protected fun fillBytes(
        planes: Array<Image.Plane>,
        yuvBytes: Array<ByteArray?>
    ) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer[yuvBytes[i]]
        }
    }


    //TODO rotate image if image captured on sumsong devices
    //Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    fun rotateBitmap(input: Bitmap): Bitmap? {
        Log.d("trySensor", sensorOrientation.toString() + "     " + getScreenOrientation())
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(sensorOrientation.toFloat())
        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        //TODO show live camera footage
//        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            //TODO show live camera footage
//            setFragment()
//        } else {
//            finish()
//        }
//    }

//        //TODO ask for permission of camera upon first launch of application
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
//            ) {
//                val permission = arrayOf(
//                    Manifest.permission.CAMERA
//                )
//                requestPermissions(permission, 1122)
//            } else {
//                //TODO show live camera footage
//                setFragment()
//            }
//        } else {
//            //TODO show live camera footage
//            setFragment()
//        }