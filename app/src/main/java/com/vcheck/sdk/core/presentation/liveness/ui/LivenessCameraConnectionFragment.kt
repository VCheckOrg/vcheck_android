package com.vcheck.sdk.core.presentation.liveness.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Camera Connection Fragment that captures images from camera.
 * Instantiated by newInstance.
 */
//@SuppressLint("ValidFragment")
class LivenessCameraConnectionFragment() : Fragment() {

    private var cameraConnectionCallback: ConnectionCallback? = null
    private var imageListener: OnImageAvailableListener? = null

    companion object {
        private const val FRAGMENT_DIALOG = "camera_dialog"

        //removed chooseOptimalSize() !
        fun newInstance(
            callback: ConnectionCallback,
            imageListener: OnImageAvailableListener)
        : LivenessCameraConnectionFragment {
            val fragment = LivenessCameraConnectionFragment()
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
    private var cameraId: String? = null
    private var textureView: AutoFitTextureView? = null
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

        return inflater.inflate(R.layout.liveness_camera_fragment, container, false)
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
        val activity = activity as VCheckLivenessActivity
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
                val activity = activity as VCheckLivenessActivity
                val manager =
                    activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                try {
                    if (!cameraOpenCloseLock.tryAcquire(4500, TimeUnit.MILLISECONDS)) {
                        ErrorDialog.newInstance("Opening Camera from lock has not been triggered.")
                            .show(childFragmentManager, FRAGMENT_DIALOG)
                    }
                    if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance("Camera permission not granted. " +
                                "Please, go to Settings and grant Camera permission for this app.")
                            .show(childFragmentManager, FRAGMENT_DIALOG)
                    }
                    manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
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
            texture.setDefaultBufferSize(previewSize!!.width / 2, previewSize!!.height / 2)

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
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }
                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder!!.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            // Flash is automatically enabled when necessary - disabled!
//                            previewRequestBuilder!!.set(
//                                CaptureRequest.CONTROL_AE_MODE,
//                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                            // Finally, we start displaying the camera preview.
                            previewRequest = previewRequestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(
                                previewRequest!!, captureCallback, backgroundHandler)
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
            val activity = activity as VCheckLivenessActivity
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
}