package com.vcheck.demo.dev.presentation.segmentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.params.MeteringRectangle
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.presentation.liveness.ui.AutoFitTextureView
import com.vcheck.demo.dev.presentation.segmentation.VCheckSegmentationActivity
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


/**
 * Camera Connection Fragment that captures images from camera.
 * Instantiated by newInstance.
 */
//@SuppressLint("ValidFragment")
class SegmentationCameraConnectionFragment() : Fragment() {

    private var cameraConnectionCallback: ConnectionCallback? = null
    private var imageListener: OnImageAvailableListener? = null

    private var mCameraManager: CameraManager? = null
    private var mCameraCharacteristics: CameraCharacteristics? = null
    private var mManualFocusEngaged: Boolean = false

    companion object {
        private const val FRAGMENT_DIALOG = "camera_dialog"
        private const val CLICK_THRESHOLD = 200
        private const val FOCUS_TAG: String = "FOCUS_TAG"

        //removed chooseOptimalSize() !
        fun newInstance(
            callback: ConnectionCallback,
            imageListener: OnImageAvailableListener)
                : SegmentationCameraConnectionFragment {
            val fragment = SegmentationCameraConnectionFragment()
            fragment.cameraConnectionCallback = callback
            fragment.imageListener = imageListener
            return fragment
        }
    }

    /** A [Semaphore] to prevent the app from exiting before closing the camera.  */
    private val cameraOpenCloseLock =
        Semaphore(1)

    private val captureCallback: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult) {
            //Stub
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult) {
            //Stub
        }
    }
    var cameraId: String? = null
    var textureView: AutoFitTextureView? = null

    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var sensorOrientation: Int? = null
    private var previewSize: Size? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            texture: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(
            texture: SurfaceTexture, width: Int, height: Int) {
        }
        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }
        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }
    private var previewReader: ImageReader? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cd: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release()
            cameraDevice = cd
            createCameraPreviewSession()
        }

        override fun onDisconnected(cd: CameraDevice) {
            cameraOpenCloseLock.release()
            cd.close()
            cameraDevice = null
        }

        override fun onError(cd: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cd.close()
            cameraDevice = null
            val activity = activity
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.segmentation_camera_fragment, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?) {
        textureView = view.findViewById<View>(R.id.texture) as AutoFitTextureView
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    fun setCamera(cameraId: String?) {
        this.cameraId = cameraId
    }

    /** Sets up member variables related to camera.  */
    private fun setUpCameraOutputs() {
        val activity = activity as VCheckSegmentationActivity
        try {
            sensorOrientation = 270 //CameraCharacteristics.SENSOR_ORIENTATION

            previewSize = activity.streamSize

            textureView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)

        } catch (e: CameraAccessException) {
            ErrorDialog.newInstance("Camera access error")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the device this code runs.
            ErrorDialog.newInstance("Camera2 API is not supported on this device.")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }
        cameraConnectionCallback!!.onPreviewSizeChosen(previewSize, sensorOrientation!!)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        setUpCameraOutputs()
        //configureTransform(width, height)

        textureView!!.post {
            backgroundHandler!!.post {
                val activity = activity as VCheckSegmentationActivity
                mCameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                try {
                    if (!cameraOpenCloseLock.tryAcquire(4500, TimeUnit.MILLISECONDS)) {
                        ErrorDialog.newInstance("Opening Camera from lock has not been triggered.")
                            .show(childFragmentManager, FRAGMENT_DIALOG)
                    }
                    if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(
                            "Camera permission not granted. " +
                                    "Please, go to Settings and grant Camera permission for this app.")
                            .show(childFragmentManager, FRAGMENT_DIALOG)
                    }
                    mCameraManager!!.openCamera(cameraId!!, stateCallback, backgroundHandler)
                } catch (e: CameraAccessException) {
                    ErrorDialog.newInstance("Camera access error")
                        .show(childFragmentManager, FRAGMENT_DIALOG)
                } catch (e: InterruptedException) {
                    ErrorDialog.newInstance("Interrupted while trying to lock camera opening.")
                        .show(childFragmentManager, FRAGMENT_DIALOG)
                }
            }
        }
    }

    /** Closes the current [CameraDevice].  */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            if (null != captureSession) {
                captureSession!!.close()
                captureSession = null
            }
            if (null != cameraDevice) {
                cameraDevice!!.close()
                cameraDevice = null
            }
            if (null != previewReader) {
                previewReader!!.close()
                previewReader = null
            }
        } catch (e: InterruptedException) {
            ErrorDialog.newInstance("Interrupted while trying to lock camera closing.")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /** Starts a background thread and its [Handler].  */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("ImageListener")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    /** Stops the background thread and its [Handler].  */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            ErrorDialog.newInstance("Interrupted while stopping background thread.")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }
    }

    /** Creates a new [CameraCaptureSession] for camera preview.  */
    private fun createCameraPreviewSession() {
        try {
            val texture = textureView!!.surfaceTexture!!

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height) // was /2 !

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder!!.addTarget(surface)

            // Create the reader for the preview frames.
            previewReader = ImageReader.newInstance(
                previewSize!!.width, previewSize!!.height, ImageFormat.YUV_420_888, 3) //!
            previewReader!!.setOnImageAvailableListener(imageListener, backgroundHandler)

            previewRequestBuilder!!.addTarget(previewReader!!.surface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice!!.createCaptureSession(
                listOf(surface, previewReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }
                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            previewRequest = previewRequestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(
                                previewRequest!!, captureCallback, backgroundHandler)

                            textureView!!.setOnTouchListener { v, event ->
                                Log.d("SEG", "======= TOUCH EVENT: ${event.action}")
                                val duration = event.eventTime - event.downTime
                                if (event.action == MotionEvent.ACTION_UP && duration < CLICK_THRESHOLD) {
                                    Log.d("SEG", "======= TOUCH EVENT SATISFIED PREDICATION!")
                                    setFocusArea(event.x.toInt(), event.y.toInt())
                                }
                                true
                            }
                        } catch (e: CameraAccessException) {
                            ErrorDialog.newInstance("Camera access exception occured from onConfigured()")
                                .show(childFragmentManager, FRAGMENT_DIALOG)
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        ErrorDialog.newInstance("Failed to configure camera")
                            .show(childFragmentManager, FRAGMENT_DIALOG)
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            ErrorDialog.newInstance("Camera access is required, but got error")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }
    }

    /**
     * Callback for Activities to use to initialize their data once the selected preview size is
     * known.
     */
    interface ConnectionCallback {
        fun onPreviewSizeChosen(size: Size?, cameraRotation: Int)
    }

    /** Shows an error message dialog.  */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity as VCheckSegmentationActivity
            return AlertDialog.Builder(activity)
                .setMessage(arguments?.getString(ARG_MESSAGE))
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    //activity.finish() //!
                    dismiss()
                }
                .create()
        }
        companion object {
            private const val ARG_MESSAGE = "message"
            fun newInstance(message: String?): ErrorDialog {
                val dialog =
                    ErrorDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.arguments = args
                return dialog
            }
        }
    }

    @Throws(CameraAccessException::class)
    private fun setFocusArea(focus_point_x: Int, focus_point_y: Int) {
        if (cameraId == null || mManualFocusEngaged) return
        if (mCameraManager == null) {
            mCameraManager = (activity as VCheckSegmentationActivity)
                .getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }
        var focusArea: MeteringRectangle? = null
        if (mCameraManager != null) {
            if (mCameraCharacteristics == null) {
                mCameraCharacteristics = mCameraManager!!.getCameraCharacteristics(cameraId!!)
            }
            val sensorArraySize: Rect = mCameraCharacteristics!!.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!

            val y = (focus_point_x.toFloat() /
                    textureView!!.layoutParams.width * sensorArraySize.height().toFloat()).toInt() //currentWidth ??
            val x = (focus_point_y.toFloat() /
                    textureView!!.layoutParams.height * sensorArraySize.width().toFloat()).toInt() //currentHeight ??
            val halfTouchLength = 150

            focusArea = MeteringRectangle(
                Math.max(x - halfTouchLength, 0),
                Math.max(y - halfTouchLength, 0),
                halfTouchLength * 2,
                halfTouchLength * 2,
                MeteringRectangle.METERING_WEIGHT_MAX - 1)
        }
        val mCaptureCallback: CaptureCallback = object : CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                mManualFocusEngaged = false
                if (request.tag == FOCUS_TAG) { // previously getTag == "Focus_tag"
                    //the focus trigger is complete -
                    //resume repeating (preview surface will get frames), clear AF trigger
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_IDLE
                    )
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
                    )
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        null
                    ) // As documentation says AF_trigger can be null in some device
                    try {
                        captureSession!!.setRepeatingRequest(
                            previewRequestBuilder!!.build(),
                            null,
                            backgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        // error handling
                    }
                }
            }

            override fun onCaptureFailed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                failure: CaptureFailure
            ) {
                super.onCaptureFailed(session, request, failure)
                mManualFocusEngaged = false
            }
        }
        captureSession!!.stopRepeating() // Destroy current session
        previewRequestBuilder!!.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CameraMetadata.CONTROL_AF_TRIGGER_IDLE
        )
        previewRequestBuilder!!.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CameraMetadata.CONTROL_AF_TRIGGER_START
        )
        captureSession!!.capture(
            previewRequestBuilder!!.build(),
            mCaptureCallback,
            backgroundHandler
        ) //Set all settings for once
        if (isMeteringAreaAESupported()) {
            previewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(focusArea))
        }
        if (isMeteringAreaAFSupported()) {
            previewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(focusArea))
            previewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_AUTO
            )
        }
        previewRequestBuilder!!.setTag(FOCUS_TAG) //it will be checked inside mCaptureCallback
        captureSession!!.capture(
            previewRequestBuilder!!.build(),
            mCaptureCallback,
            backgroundHandler
        )
        mManualFocusEngaged = true
    }


    private fun isMeteringAreaAFSupported(): Boolean { // AF stands for AutoFocus
        val afRegion: Int = mCameraCharacteristics!!.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)!!
        return afRegion >= 1
    }


    private fun isMeteringAreaAESupported(): Boolean { //AE stands for AutoExposure
        val aeState: Int = mCameraCharacteristics!!.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)!!
        return aeState >= 1
    }

}

// ------ Deprecated/obsolete logic:

//    override fun onDestroy() {
//        closeCamera()
//        stopBackgroundThread()
//        super.onDestroy()
//    }

// Deprecated from setupCameraOutputs():
//          val manager =
//            activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//          val characteristics = manager.getCameraCharacteristics(cameraId!!)
//            val map =
//                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
// Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
// bus' bandwidth limitation, resulting in gorgeous previews but the storage of
// garbage capture data.
//            previewSize = chooseOptimalSize(
//                map!!.getOutputSizes(SurfaceTexture::class.java),
//                inputSize.width,
//                inputSize.height)
//Log.d("Ok", "------------ SIZE: width ${previewSize!!.width} | height: ${previewSize!!.height}")

/** Compares two `Size`s based on their areas.  */
//internal class CompareSizesByArea : Comparator<Size?> {
//    override fun compare(lhs: Size?, rhs: Size?): Int {
//        return java.lang.Long.signum(
//            lhs!!.width.toLong() * lhs.height - rhs!!.width.toLong() * rhs.height)
//    }
//}

/**
 * The camera preview size will be chosen to be the smallest frame by pixel size capable of
 * containing a DESIRED_SIZE x DESIRED_SIZE square.
 */
//private const val MINIMUM_PREVIEW_SIZE = 640  // was 320 by default

/** Conversion from screen rotation to JPEG orientation.  */
//private val ORIENTATIONS = SparseIntArray()


//    @SuppressLint("ValidFragment") constructor(
//
//        /** A [OnImageAvailableListener] to receive frames as they are available.  */
//
//        /** The layout identifier to inflate for this Fragment.  */
//
//        /** The input size in pixels desired by TensorFlow (width and height of a square bitmap).  */
//        //private val inputSize: Size
//    ) : this()

/**
 * Configures the necessary [Matrix] transformation to `mTextureView`. This method should be
 * called after the camera preview size is determined in setUpCameraOutputs and also the size of
 * `mTextureView` is fixed.
 *
 * @param viewWidth The width of `mTextureView`
 * @param viewHeight The height of `mTextureView`
 */
//    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
//        val activity = activity
//        if (null == textureView || null == previewSize || null == activity) {
//            return
//        }
//        //val rotation = activity.windowManager.defaultDisplay.rotation
//        val matrix = Matrix()
//        val viewRect = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
//        val bufferRect = RectF(
//            0F, 0F,
//            previewSize!!.height.toFloat(),
//            previewSize!!.width.toFloat())
//        val centerX = viewRect.centerX()
//        val centerY = viewRect.centerY()
//        //Log.d("Ok", "============ ROTATION: $rotation")
////        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
////            Log.d("Ok", "============ ROTATION: Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation")
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
//            val scale = max(
//                viewHeight.toFloat() / previewSize!!.height,
//                viewWidth.toFloat() / previewSize!!.width)
//            matrix.postScale(scale, scale, centerX, centerY)
//            //matrix.postRotate(90 * (rotation - 2).toFloat(), centerX, centerY)
////        } else if (Surface.ROTATION_180 == rotation) {
////            matrix.postRotate(180f, centerX, centerY)
////        }
//        textureView!!.setTransform(matrix)
//    }

/**
 * Given `choices` of `Size`s supported by a camera, chooses the smallest one whose
 * width and height are at least as large as the minimum of both, or an exact match if possible.
 *
 * @param choices The list of sizes that the camera supports for the intended output class
 * @param width The minimum desired width
 * @param height The minimum desired height
 * @return The optimal `Size`, or an arbitrary one if none were big enough
 */
//        protected fun chooseOptimalSize(
//            choices: Array<Size>,
//            width: Int,
//            height: Int
//        ): Size {
//            val minSize = Math.max(
//                Math.min(width, height),
//                MINIMUM_PREVIEW_SIZE
//            )
//            val desiredSize = Size(width, height)
//
//            // Collect the supported resolutions that are at least as big as the preview Surface
//            var exactSizeFound = false
//            val bigEnough: MutableList<Size> =
//                ArrayList()
//            val tooSmall: MutableList<Size> =
//                ArrayList()
//            for (option in choices) {
//                if (option == desiredSize) {
//                    // Set the size but don't return yet so that remaining sizes will still be logged.
//                    exactSizeFound = true
//                }
//                if (option.height >= minSize && option.width >= minSize) {
//                    bigEnough.add(option)
//                } else {
//                    tooSmall.add(option)
//                }
//            }
//            if (exactSizeFound) {
//                return desiredSize
//            }
//
//            // Pick the smallest of those, assuming we found any
//            return if (bigEnough.size > 0) {
//                // LOGGER.i("Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
//                Collections.min(
//                    bigEnough,
//                    CompareSizesByArea()
//                )
//            } else {
//                // LOGGER.e("Couldn't find any suitable preview size");
//                choices[0]
//            }
//        }