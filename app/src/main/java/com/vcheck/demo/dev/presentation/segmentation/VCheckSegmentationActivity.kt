package com.vcheck.demo.dev.presentation.segmentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.ActivityVcheckSegmentationBinding
import com.vcheck.demo.dev.di.VCheckDIContainer
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.presentation.liveness.flow_logic.LivenessCameraParams
import com.vcheck.demo.dev.presentation.segmentation.flow_logic.*
import com.vcheck.demo.dev.presentation.segmentation.ui.SegmentationCameraConnectionFragment
import com.vcheck.demo.dev.presentation.transferrable_objects.CheckPhotoDataTO
import com.vcheck.demo.dev.util.VCheckContextUtils
import com.vcheck.demo.dev.util.setMargins
import com.vcheck.demo.dev.util.vibrateDevice
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.concurrent.fixedRateTimer


class VCheckSegmentationActivity : AppCompatActivity(),
    ImageReader.OnImageAvailableListener {

    companion object {
        const val TAG = "SegmentationActivity"
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 60000 //max is 60s
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 2000 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 500
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
    }

    //private var segmentationResponse: MutableLiveData<Resource<SegmentationGestureResponse>> = MutableLiveData()

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

    private var currentCheckBitmap: Bitmap? = null

    //currently using 1st(+2nd) photo caching instead of putting them to array/list
    private var photo1FullBitmap: Bitmap? = null
    private var photo2FullBitmap: Bitmap? = null

    //TODO finish text colors!
    private fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding!!.livenessActivityBackground.setBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVcheckSegmentationBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        changeColorsToCustomIfPresent()

        onBackPressedDispatcher.addCallback {
            //Stub; no back press needed throughout liveness flow
        }
        binding!!.closeIconBtn.setOnClickListener {
            this@VCheckSegmentationActivity.finish()
        }

        setDocData()
        setCameraFragment()
        setupInstructionStageUI()
    }

    private fun setupDocCheckStage() {
        binding!!.scalableDocHandView.isVisible = false
        binding!!.readyButton.isVisible = false
        resetFlowForNewSession()
        setUIForNextStage()
    }

    private fun setDocData() {
        docData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()!!
    }

    private fun resetFlowForNewSession() {
        photo1FullBitmap = null
        photo2FullBitmap = null
        blockProcessingByUI = false
        blockRequestByProcessing = false
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        isLivenessSessionFinished = false
        checkedDocIdx = 0
        setGestureRequestDebounceTimer()
    }

    private fun areAllDocPagesChecked(): Boolean {
        return checkedDocIdx >= docData.maxPagesCount
    }

    private fun setGestureRequestDebounceTimer() {
        fixedRateTimer("timer", false, 0L, GESTURE_REQUEST_DEBOUNCE_MILLIS) {
            if (!isLivenessSessionFinished) {
                if (areAllDocPagesChecked()) {
                    finishLivenessSession(true)
                } else {
                    if (!enoughTimeForNextCheck()) {
                        onFatalObstacleWorthRetry(R.id.action_dummySegmentationStartFragment_to_segTimeoutFragment)
                    } else if (!isLivenessSessionFinished
                        && !blockProcessingByUI
                        && !blockRequestByProcessing
                        && enoughTimeForNextCheck()
                        && currentCheckBitmap != null) {

                        blockProcessingByUI = true
                        blockRequestByProcessing = true

                        CalcThread().run {
                            determineImageResult()
                        }
                    }
                }
            }
        }
    }

    private fun finishLivenessSession(withActivityFinish: Boolean) {
        isLivenessSessionFinished = true
        currentCheckBitmap = null

        if (withActivityFinish) {
            var photo1Path: String? = null
            var photo2Path: String? = null

            photo1FullBitmap?.let {
                photo1Path = createTempFileForBitmapFrame(it)
            }
            photo2FullBitmap?.let {
                photo2Path = createTempFileForBitmapFrame(it)
            }

            VCheckDIContainer.mainRepository.setCheckDocPhotosTO(CheckPhotoDataTO(
                docCategoryIdxToType(docData.category), photo1Path!!, photo2Path))

            finish()
        }
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
        supportFragmentManager.beginTransaction().replace(R.id.seg_container, fragment).commit()

        setSegmentationFrameSize()
    }

    override fun onImageAvailable(reader: ImageReader?) {
        //calling verbose extension function, which leads to processImage()
        if (!isLivenessSessionFinished) {
            onImageAvailableImpl(reader)
        }
    }

    private fun setSegmentationFrameSize() {
        if (frameSize == null) {
            val displayMetrics: DisplayMetrics = resources.displayMetrics
            val factor: Float = displayMetrics.density
            val dpWidth = displayMetrics.widthPixels / factor

            val frameWidth = ((dpWidth * 0.88) * factor).toInt()
            val frameHeight = (frameWidth * 0.63).toInt()

//            Log.d("SEG", "VIEW WIDTH: $dpWidth")
//            Log.d("SEG", "FRAME WIDTH: $frameWidth | FRAME HEIGHT: $frameHeight")

            frameSize = Size(frameWidth, frameHeight)

            binding!!.segmentationFrame.layoutParams.width = frameSize!!.width
            binding!!.segmentationFrame.layoutParams.height = frameSize!!.height

            binding!!.segmentationMaskWrapper.post {
                binding!!.segmentationMaskWrapper.setRectHoleSize(
                    frameSize!!.width - 6, frameSize!!.height - 6)
            }
            binding!!.docAnimationView.post {
                binding!!.docAnimationView.layoutParams.width = frameSize!!.width
                binding!!.docAnimationView.layoutParams.height = frameSize!!.height
            }
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
                    //caching full image bitmap (is order right??)
                    currentCheckBitmap = finalBitmap
                    //recycling bitmap:
                    rgbFrameBitmap!!.recycle()
                    //running post-inference callback
                    postInferenceCallback!!.run()
                    //updating cached bitmap for gesture request
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

        blockRequestByProcessing = true

        val fullBitmap: Bitmap = currentCheckBitmap!!
        val minimizedBitmap: Bitmap = fullBitmap.cropWithMask()

        //saveImageToGallery(minimizedBitmap, this@VCheckSegmentationActivity, "check") //remove!

        val file = File(createTempFileForBitmapFrame(minimizedBitmap))
        val image: MultipartBody.Part = MultipartBody.Part.createFormData(
            "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))

        val response = VCheckDIContainer.mainRepository.sendSegmentationDocAttempt(
                image,
                docData.country,
                docData.category.toString(),
                checkedDocIdx.toString())

        if (response != null) {
            processCheckResult(response, fullBitmap, checkedDocIdx)
        } else {
            Log.d(TAG, "========= --- RESPONSE FOR IDX IS NULL!")
        }
    }

    private fun processCheckResult(it: SegmentationGestureResponse,
                                    fullBitmap: Bitmap,
                                    currentPageIdx: Int) {
        runOnUiThread {
            Log.d(TAG, "========= GOT RESPONSE FOR IDX __ $currentPageIdx __ : ${it.success}")
            if (it.success) {
                vibrateDevice(this@VCheckSegmentationActivity,
                    STAGE_VIBRATION_DURATION_MILLIS)
                if (currentPageIdx == 0 && photo1FullBitmap == null) {
                    photo1FullBitmap = fullBitmap
                }
                if (currentPageIdx == 1 && photo2FullBitmap == null) {
                    photo2FullBitmap = fullBitmap
                }
                if (!areAllDocPagesChecked() && currentPageIdx == checkedDocIdx) {
                    indicateNextStage()
                    checkedDocIdx += 1
                    blockRequestByProcessing = false
                }
            } else {
                blockProcessingByUI = false
                blockRequestByProcessing = false
            }
            if (it.errorCode != 0) {
                showSingleToast("Scan response error: [${it.errorCode}]")
            }
        }
    }

    /// -------------------------------------------- UI functions

    private fun setupInstructionStageUI() {

        blockProcessingByUI = true
        blockRequestByProcessing = true
        binding!!.docAnimationView.isVisible = false
        binding!!.scalableDocHandView.isVisible = true

        when(docCategoryIdxToType(docData.category)) {
            DocType.ID_CARD -> {
                binding!!.scalableDocHandView.background = AppCompatResources.getDrawable(
                    this@VCheckSegmentationActivity, R.drawable.img_hand_id_card)
            }
            DocType.FOREIGN_PASSPORT -> {
                binding!!.scalableDocHandView.background = AppCompatResources.getDrawable(
                    this@VCheckSegmentationActivity, R.drawable.img_hand_foreign_passport)
            } else -> {
                binding!!.scalableDocHandView.background = AppCompatResources.getDrawable(
                    this@VCheckSegmentationActivity, R.drawable.img_hand_inner_passport)
            }
        }

        animateInstructionStage()

        binding!!.readyButton.setOnClickListener {
            setupDocCheckStage()
        }
    }

    private fun indicateNextStage() {

        binding!!.tvSegmentationInstruction.setMargins(
            20, 45, 20, 20)
        binding!!.tvSegmentationInstruction.setText(R.string.segmentation_stage_success)

        if (docCategoryIdxToType(docData.category) == DocType.ID_CARD) {
            binding!!.docAnimationView.isVisible = true
            binding!!.docAnimationView.playAnimation()
        } else {
            binding!!.docAnimationView.isVisible = false
        }

        Handler(Looper.getMainLooper()).postDelayed ({
            setUIForNextStage()
        }, BLOCK_PIPELINE_TIME_MILLIS)
    }

    private fun setUIForNextStage() {

        binding!!.docAnimationView.isVisible = false
        binding!!.tvSegmentationInstruction.setMargins(
            20, 45, 20, 20)

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
    }

    private fun animateInstructionStage() {

        binding!!.scalableDocHandView.apply {
            val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", 3f)
            val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", 3f)
            scaleUpX.duration = 1000
            scaleUpY.duration = 1000
            scaleUpX.repeatCount = ObjectAnimator.INFINITE
            scaleUpY.repeatCount = ObjectAnimator.INFINITE

            val moveLeftX: ObjectAnimator = ObjectAnimator.ofFloat(this,
            "translationX", -350F)
            val moveUpY: ObjectAnimator = ObjectAnimator.ofFloat(this,
                "translationY", -200F)
            moveLeftX.duration = 1000
            moveUpY.duration = 1000
            moveLeftX.repeatCount = ObjectAnimator.INFINITE
            moveUpY.repeatCount = ObjectAnimator.INFINITE

            val scaleUp = AnimatorSet()
            val moveDiag = AnimatorSet()

            scaleUp.play(scaleUpX).with(scaleUpY)
            moveDiag.play(moveUpY).with(moveLeftX)

            scaleUp.start()
            moveDiag.start()
        }
    }

    private fun onFatalObstacleWorthRetry(actionIdForNav: Int) {
        vibrateDevice(this@VCheckSegmentationActivity,
            STAGE_VIBRATION_DURATION_MILLIS)
        finishLivenessSession(false)
        livenessSessionLimitCheckTime = SystemClock.elapsedRealtime()
        safeNavigateToResultDestination(actionIdForNav)
    }

    private fun safeNavigateToResultDestination(actionIdForNav: Int) {
        try {
            findNavController(R.id.segmentation_host_fragment).navigate(actionIdForNav)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Attempt of nav to major obstacle was made, but was already on another fragment")
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Caught exception: Liveness Activity does not have a NavController set!")
        } catch (e: Exception) {
            showSingleToast(e.message)
        }
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