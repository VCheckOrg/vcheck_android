package com.vcheck.demo.dev.presentation.segmentation

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.media.ImageReader
import android.os.*
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.databinding.ActivityVcheckSegmentationBinding
import com.vcheck.demo.dev.di.VCheckDIContainer
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.presentation.liveness.flow_logic.LivenessCameraParams
import com.vcheck.demo.dev.presentation.liveness.ui.LivenessCameraConnectionFragment
import com.vcheck.demo.dev.presentation.segmentation.flow_logic.*
import com.vcheck.demo.dev.presentation.segmentation.ui.SegmentationCameraConnectionFragment
import com.vcheck.demo.dev.presentation.transferrable_objects.CheckPhotoDataTO
import com.vcheck.demo.dev.util.VCheckContextUtils
import com.vcheck.demo.dev.util.pixelsToDp
import com.vcheck.demo.dev.util.vibrateDevice
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.concurrent.fixedRateTimer


class VCheckSegmentationActivity : AppCompatActivity(),
    ImageReader.OnImageAvailableListener {

    companion object {
        const val TAG = "LivenessActivity"
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 60000 //max is 60s
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 800 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 500
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
    }

    //TODO add timeout 60s!

    private var segmentationResponse: MutableLiveData<Resource<SegmentationGestureResponse>> = MutableLiveData()

    private var binding: ActivityVcheckSegmentationBinding? = null
    private var mToast: Toast? = null

    var streamSize: Size = Size(640, 480)

    private var frameSize: Size? = null

    private lateinit var docData: DocTypeData

    private var checkedDocIdx = 0

    var openLivenessCameraParams: LivenessCameraParams? = LivenessCameraParams()

    private var camera2Fragment: SegmentationCameraConnectionFragment? = null

    private var livenessSessionLimitCheckTime: Long = 0
    private var isLivenessSessionFinished: Boolean = false
    private var blockProcessingByUI: Boolean = false
    private var blockRequestByProcessing: Boolean = false

    private var currentCheckBitmap: Bitmap? = null //!

    private var fullSizeBitmapList: ArrayList<Bitmap> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVcheckSegmentationBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding!!.livenessActivityBackground.setBackgroundColor(Color.parseColor(it))
        }

        onBackPressedDispatcher.addCallback {
            //Stub; no back press needed throughout liveness flow
        }

        setCameraFragment()
        setupInstructionStage()
    }

    private fun setupInstructionStage() {
        blockProcessingByUI = true
        blockRequestByProcessing = true

        binding!!.scalableDocHandImage.isVisible = true

        //TODO animate instruction pictures!

        binding!!.readyButton.setOnClickListener {
            setupDocCheckStage()
        }
    }

    private fun indicateNextStage() {

        binding!!.tvSegmentationInstruction.setText(R.string.segmentation_stage_success)

        //TODO

        Handler(Looper.getMainLooper()).postDelayed ({
            setUIForNextStage()
        }, BLOCK_PIPELINE_TIME_MILLIS)
    }


    private fun setUIForNextStage() {
        when(docCategoryIdxToType(docData.category)) {
            DocType.FOREIGN_PASSPORT -> {
                binding!!.tvSegmentationInstruction.setText(R.string.segmentation_single_page_hint)
            }
            DocType.ID_CARD -> {
                if (checkedDocIdx == 0) {
                    binding!!.tvSegmentationInstruction.setText(R.string.segmentation_front_side_hint)
                }
                if (checkedDocIdx == 1) {
                    binding!!.tvSegmentationInstruction.setText(R.string.segmentation_back_side_hint)
                }
            }
            else -> {
                if (checkedDocIdx == 0) {
                    binding!!.tvSegmentationInstruction.setText(R.string.segmentation_front_side_hint)
                }
                if (checkedDocIdx == 1) {
                    binding!!.tvSegmentationInstruction.setText(R.string.segmentation_back_side_hint)
                }
            }
        }
        blockProcessingByUI = false
        blockRequestByProcessing = false
    }

    private fun setupDocCheckStage() {
        binding!!.scalableDocHandImage.isVisible = false
        binding!!.readyButton.isVisible = false
        setDocData()
        resetFlowForNewSession()
        setGestureResponsesObserver()
    }

    private fun setDocData() {
        docData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()!!
    }

    private fun resetFlowForNewSession() {
        blockProcessingByUI = false
        blockRequestByProcessing = false
        checkedDocIdx = 0
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        isLivenessSessionFinished = false
        setGestureRequestDebounceTimer()
    }

    private fun areAllDocPagesChecked(): Boolean {
        return checkedDocIdx >= docData.maxPagesCount
    }

    private fun setGestureRequestDebounceTimer() {
        fixedRateTimer("timer", false, 0L, GESTURE_REQUEST_DEBOUNCE_MILLIS) {
            determineImageResult()
        }
    }

    private fun setGestureResponsesObserver() {
        Handler(Looper.getMainLooper()).post {
            segmentationResponse.observe(this@VCheckSegmentationActivity) {
                //blockRequestByProcessing = false
                runOnUiThread {
                    Log.d(TAG, "============== GOT RESPONSE: ${it.data}")
                    if (!isLivenessSessionFinished) {
                        if (areAllDocPagesChecked()) {
                            Log.d(TAG, "==============++++++++++++++++  PASSED ALL PAGES!")
                            finishLivenessSession()
                            //delayedNavigateOnLivenessSessionEnd() //!
                        } else {
                            if (it.data != null && it.data.success) {
                                Log.d(TAG, "============== PASSED DOC PAGE: $checkedDocIdx")
                                checkedDocIdx += 1
                                if (!areAllDocPagesChecked()) {
                                    fullSizeBitmapList.add(currentCheckBitmap!!) //!
                                    blockProcessingByUI = true
                                    indicateNextStage()
                                }
                            } else {
                                blockProcessingByUI = false
                                blockRequestByProcessing = false
                            }
                            if (it.data != null && it.data.errorCode != 0) {
                                //TODO: add error handling
                                showSingleToast("GESTURE CHECK ERROR: [${it.data.errorCode}]")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun finishLivenessSession() {
        isLivenessSessionFinished = true
        currentCheckBitmap = null

        var photo1Path: String? = null
        var photo2Path: String? = null

        fullSizeBitmapList.forEachIndexed { index, bitmap ->
            if (index == 0) {
                photo1Path = createTempFileForBitmapFrame(bitmap)
            }
            if (index == 1) {
                photo2Path = createTempFileForBitmapFrame(bitmap)
            }
        }

        VCheckDIContainer.mainRepository.setCheckDocPhotosTO(CheckPhotoDataTO(
            docCategoryIdxToType(docData.category), photo1Path!!, photo2Path))
        finish()
    }

    private fun enoughTimeForNextCheck(): Boolean {
        return SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime <= LIVENESS_TIME_LIMIT_MILLIS
    }

    private fun setCameraFragment() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraId: String? = null
        try {
            for (cameraIdx in cameraManager.cameraIdList) {
                val chars: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdx)
                if (CameraCharacteristics.LENS_FACING_BACK == chars.get(CameraCharacteristics.LENS_FACING)) {
                    cameraId = cameraIdx
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "BACK CAMERA DETECTION ERROR: ${e.message}")
            showSingleToast(e.message)
        }

        camera2Fragment = SegmentationCameraConnectionFragment.newInstance(
            object : SegmentationCameraConnectionFragment.ConnectionCallback {
                override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                    openLivenessCameraParams?.previewHeight = size!!.height
                    openLivenessCameraParams?.previewWidth = size.width
                    openLivenessCameraParams?.sensorOrientation = cameraRotation - getScreenOrientation()
                }
            },
            this@VCheckSegmentationActivity)



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

    fun processImage() {
        try {
            //if (!blockProcessingByUI && !blockRequestByProcessing) {
            Handler(Looper.getMainLooper()).post {
                openLivenessCameraParams?.apply {

                    if (frameSize == null) {

                        //TODO test!
                        val desiredWidth = previewWidth.toFloat().pixelsToDp(this@VCheckSegmentationActivity).toInt()
                       // val desiredHeight = previewHeight.toFloat().pixelsToDp(this@VCheckSegmentationActivity).toInt()

                        val frameWidth = (desiredWidth * 0.76).toInt()
                        val frameHeight = (frameWidth * 0.63).toInt()

                        frameSize = Size(frameWidth, frameHeight)

                        binding!!.segmentationFrame.layoutParams.width = frameWidth
                        binding!!.segmentationFrame.layoutParams.height = frameHeight
                    }

                    imageConverter!!.run()
                    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
                    rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)

                    currentCheckBitmap = rgbFrameBitmap!! //!!!

                    //val finalBitmap = rotateBitmap(rgbFrameBitmap!!)!!
                    //caching bitmap to array/list:
                    //fullSizeBitmapList.add(finalBitmap)
                    //TODO cache as image!
                    //recycling bitmap:
                    //rgbFrameBitmap!!.recycle() //TODO recycle?
                    //running post-inference callback
                    postInferenceCallback!!.run()
                    //updating cached bitmap for gesture request
                    //currentCheckBitmap = finalBitmap
                }
                //}
            }
        } catch (e: Exception) {
            showSingleToast(e.message)
            showSingleToast("[TEST-processImage ex]: ${e.message}")
        } catch (e: Error) {
            showSingleToast(e.message)
            showSingleToast("[TEST-processImage err]: ${e.message}")
        }
    }

    private fun determineImageResult() {
        if (!isLivenessSessionFinished
            && !blockProcessingByUI
            && !blockRequestByProcessing
            && enoughTimeForNextCheck()) {
            if (currentCheckBitmap != null) {
                blockRequestByProcessing = true
                val minimizedBitmap = currentCheckBitmap!!.crop() //TODO test!
                val file = File(createTempFileForBitmapFrame(minimizedBitmap))
                val image: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                runOnUiThread {
                    VCheckDIContainer.mainRepository.sendSegmentationDocAttempt(
                        image,
                        docData.country,
                        docData.category.toString(),
                        checkedDocIdx.toString())
                        .observeForever {
                            Log.d(TAG, "========= WAITING FOR ANY RESPONSE FOR PAGE IDX: ${checkedDocIdx}...")
                            segmentationResponse.value = it
                        }
                }
            }
        } else {
            if (!isLivenessSessionFinished && !blockProcessingByUI) {
                runOnUiThread {
                    onFatalObstacleWorthRetry(R.id.action_dummyLivenessStartDestFragment_to_noTimeFragment)
                }
            }
        }
    }

    /// -------------------------------------------- UI functions

//    private fun animateInstructionStage() {
//        var v: View? = null
//
//        val animSetXY = AnimatorSet()
//        val y: ObjectAnimator = ObjectAnimator.ofFloat(v,
//            "translationY", v.getY(), targetY)
//        val x: ObjectAnimator = ObjectAnimator.ofFloat(v,
//            "translationX", v.getX(), targetX)
//        animSetXY.playTogether(x, y)
//        animSetXY.interpolator = LinearInterpolator(1f)
//        animSetXY.duration = 300
//        animSetXY.start()
//    }

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
        vibrateDevice(this@VCheckSegmentationActivity, STAGE_VIBRATION_DURATION_MILLIS)
        finishLivenessSession()
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        //binding!!.livenessCosmeticsHolder.isVisible = false
        safeNavigateToResultDestination(actionIdForNav)
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
        super.onDestroy()

        openLivenessCameraParams = null
    }
}