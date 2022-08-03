package com.vcheck.demo.dev.presentation.segmentation

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.addCallback
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
import com.vcheck.demo.dev.presentation.segmentation.flow_logic.createTempFileForBitmapFrame
import com.vcheck.demo.dev.presentation.segmentation.flow_logic.getScreenOrientation
import com.vcheck.demo.dev.presentation.segmentation.flow_logic.rotateBitmap
import com.vcheck.demo.dev.util.VCheckContextUtils
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
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 15000 //max is 15000
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 800 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 400
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
    }

    private var gestureResponse: MutableLiveData<Resource<SegmentationGestureResponse>> = MutableLiveData()

    private var binding: ActivityVcheckSegmentationBinding? = null
    private var mToast: Toast? = null

    var streamSize: Size = Size(640, 480)

    private lateinit var docData: DocTypeData

    private var checkedDocIdx = 0

    var openLivenessCameraParams: LivenessCameraParams? = LivenessCameraParams()

    private var camera2Fragment: LivenessCameraConnectionFragment? = null

    private var livenessSessionLimitCheckTime: Long = 0
    private var isLivenessSessionFinished: Boolean = false
    private var blockProcessingByUI: Boolean = false
    private var blockRequestByProcessing: Boolean = false

    private var gestureCheckBitmap: Bitmap? = null //!



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
        //initSetupUI()
    }

    private fun setupInstructionStage() {
        blockProcessingByUI = true
        blockRequestByProcessing = true
        binding!!.readyButton.setOnClickListener {

            setupDocCheckStage()
        }
    }

    private fun indicateNextStage() {
        binding!!.tvSegmentationInstruction.setText(R.string.segmentation_stage_success)



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
    }

    private fun setupDocCheckStage() {
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
            gestureResponse.observe(this@VCheckSegmentationActivity) {
                blockRequestByProcessing = false
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
                                    blockProcessingByUI = true
                                    indicateNextStage()
                                }
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
            this@VCheckSegmentationActivity)

        camera2Fragment!!.setCamera(cameraId)

        val fragment: Fragment = camera2Fragment!!
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun onImageAvailable(reader: ImageReader?) {
        //calling verbose extension function, which leads to processImage()
        if (!isLivenessSessionFinished) {
            //onImageAvailableImpl(reader)
        }
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
                    //bitmapArray?.add(finalBitmap)
                    //TODO cache as image!
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

    private fun determineImageResult() {
        if (!isLivenessSessionFinished
            && !blockProcessingByUI
            && !blockRequestByProcessing
            && enoughTimeForNextGesture()) {
            if (gestureCheckBitmap != null) {
                blockRequestByProcessing = true
                val file = File(createTempFileForBitmapFrame(gestureCheckBitmap!!))
                val image: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
                runOnUiThread {
                    //val currentGesture = milestoneFlow.getGestureRequestFromCurrentStage()
                    VCheckDIContainer.mainRepository.sendSegmentationDocAttempt(
                        image,
                        docData.country,
                        docData.category.toString(),
                        checkedDocIdx.toString())
                        .observeForever {
                            Log.d(TAG, "========= WAITING FOR ANY RESPONSE FOR PAGE IDX: ${checkedDocIdx}...")
                            gestureResponse.value = it
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