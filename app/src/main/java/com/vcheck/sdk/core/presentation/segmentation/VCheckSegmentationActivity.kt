package com.vcheck.sdk.core.presentation.segmentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ActivityVcheckSegmentationBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.DocType
import com.vcheck.sdk.core.domain.DocTypeData
import com.vcheck.sdk.core.domain.SegmentationGestureResponse
import com.vcheck.sdk.core.domain.docCategoryIdxToType
import com.vcheck.sdk.core.presentation.segmentation.flow_logic.*
import com.vcheck.sdk.core.presentation.transferrable_objects.CheckPhotoDataTO
import com.vcheck.sdk.core.util.VCheckContextUtils
import com.vcheck.sdk.core.util.images.BitmapUtils
import com.vcheck.sdk.core.util.setMargins
import com.vcheck.sdk.core.util.sizeInKb
import com.vcheck.sdk.core.util.vibrateDevice
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.NullPointerException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.concurrent.fixedRateTimer

@OptIn(DelicateCoroutinesApi::class)
class VCheckSegmentationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SegmentationActivity"
        private const val LIVENESS_TIME_LIMIT_MILLIS: Long = 60000 //max is 60s
        private const val BLOCK_PIPELINE_TIME_MILLIS: Long = 3800 //may reduce a bit
        private const val GESTURE_REQUEST_DEBOUNCE_MILLIS: Long = 410
        private const val IMAGE_CAPTURE_DEBOUNCE_MILLIS: Long = 200
        private const val STAGE_VIBRATION_DURATION_MILLIS: Long = 100
        private const val MASK_UI_MULTIPLY_FACTOR: Double = 1.1
        private const val VIDEO_STREAM_WIDTH_LIMIT = 800
        private const val VIDEO_STREAM_HEIGHT_LIMIT = 800
    }

    private val scope = CoroutineScope(newSingleThreadContext("segmentation"))

    private val imageCaptureExecutor = Executors.newCachedThreadPool()

    private lateinit var binding: ActivityVcheckSegmentationBinding
    private var mToast: Toast? = null

    private var streamSize: Size = Size(VIDEO_STREAM_WIDTH_LIMIT, VIDEO_STREAM_HEIGHT_LIMIT)

    private var frameSize: Size? = null

    private var apiRequestTimer: Timer? = null

    private var takeImageTimer: Timer? = null

    private lateinit var docData: DocTypeData

    private var checkedDocIdx = 0

    private var livenessSessionLimitCheckTime: Long = 0
    private var isLivenessSessionFinished: Boolean = false
    private var blockProcessingByUI: Boolean = false
    private var blockRequestByProcessing: Boolean = false

    private var currentCheckBitmap: Bitmap? = null

    var imageCapture: ImageCapture? = null

    //currently using 1st(+2nd) photo caching instead of putting them to array/list
    private var photo1FullBitmap: Bitmap? = null
    private var photo2FullBitmap: Bitmap? = null

    private fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.segActivityBackground.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.tvSegmentationInstruction.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVcheckSegmentationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        changeColorsToCustomIfPresent()

        onBackPressedDispatcher.addCallback {
            //Stub; no back press needed throughout liveness flow
        }

        setDocData()

        Handler(Looper.getMainLooper()).postDelayed({

            binding.closeIconBtn.post {
                binding.closeIconBtn.setOnClickListener {
                    onCustomBackPress()
                }
            }

            setupInstructionStageUI()

            setSegmentationFrameSize()

            setCameraProviderListener()

        }, 200)
    }

    private fun setupDocCheckStage() {
        binding.darkFrameOverlay.isVisible = false
        binding.scalableDocHandView.isVisible = false
        binding.readyButton.isVisible = false
        resetFlowForNewSession()
        setUIForNextStage()
    }

    private fun setDocData() {
        docData = VCheckDIContainer.mainRepository.getSelectedDocTypeWithData()!!
    }

    private fun resetFlowForNewSession() {
        apiRequestTimer?.cancel()
        takeImageTimer?.cancel()
        imageCapture = null
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

    @SuppressLint("RestrictedApi")
    private fun setCameraProviderListener() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this@VCheckSegmentationActivity)
        cameraProviderFuture.addListener({
            try {
                buildImageCaptureUseCase()
                bindPreview(cameraProviderFuture.get())
                setTakeImageTimer()
            } catch (e: ExecutionException) {
                showSingleToast("Error while setting camera provider: ${e.message}")
                e.printStackTrace()
            } catch (e: InterruptedException) {
                showSingleToast("Error while setting camera provider: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                showSingleToast("Unidentified Error while setting camera provider: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this@VCheckSegmentationActivity))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val preview: Preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
        try {
            binding.cameraPreviewView.viewPort?.let {
                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .setViewPort(it)
                    .build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@VCheckSegmentationActivity,
                    cameraSelector, useCaseGroup)
            }
        } catch (e: NullPointerException) {
            showSingleToast("Image Capture or View Port have not been initialized in time")
        } catch (e: Exception) {
            showSingleToast("Error while building preview: ${e.message}")
        }
    }

    @SuppressLint("RestrictedApi")
    private fun buildImageCaptureUseCase() {
        imageCapture = ImageCapture.Builder()
            .setBufferFormat(ImageFormat.YUV_420_888)
            .setMaxResolution(Size(VIDEO_STREAM_WIDTH_LIMIT, VIDEO_STREAM_HEIGHT_LIMIT))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setJpegQuality(70)
            .build()
    }

    private fun setTakeImageTimer() {
        takeImageTimer = fixedRateTimer("seg_img_capture_timer", false, 0L,
            IMAGE_CAPTURE_DEBOUNCE_MILLIS) {
            imageCapture?.takePicture(imageCaptureExecutor, object: ImageCapture.OnImageCapturedCallback() {
                @ExperimentalGetImage
                override fun onCaptureSuccess(image: ImageProxy) {
                    // Use the image, then make sure to close it.
                    //Log.d(TAG, "GOT PICTURE: W - ${image.width} | H - ${image.height}")
                    if (image.width != streamSize.width || image.height != streamSize.height) {
                        streamSize = Size(image.width, image.height)
                    }
                    if (!isLivenessSessionFinished) {
                        val bitmap = BitmapUtils.getBitmap(image)!!
                        currentCheckBitmap = bitmap
                    }
                    image.close()
                }
                override fun onError(exception: ImageCaptureException) {
                    val errorType = exception.imageCaptureError
                    Log.d(TAG, "IMG CAPTURE - TAKE PICTURE ERROR: $errorType")
                }
            })
        }
    }

    private fun setGestureRequestDebounceTimer() {
        apiRequestTimer = fixedRateTimer("seg_api_request_timer", false,
            0L, GESTURE_REQUEST_DEBOUNCE_MILLIS) {
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

                        scope.launch {
                            determineImageResult()
                        }
                    }
                }
            }
        }
    }

    fun finishWithExtra(isTimeoutToManual: Boolean, isBackPress: Boolean) {
        try {
            val data = Intent()
            data.putExtra("is_timeout_to_manual", isTimeoutToManual)
            data.putExtra("is_back_press", isBackPress)
            setResult(Activity.RESULT_OK, data)
            finish()
        } catch (e: Exception) {
            finish()
        } catch (e: Error) {
            finish()
        }
    }

    private fun finishLivenessSession(withActivityFinish: Boolean) {
        isLivenessSessionFinished = true
        currentCheckBitmap = null
        scope.cancel()
        takeImageTimer?.cancel()
        apiRequestTimer?.cancel()

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

            finishWithExtra(isTimeoutToManual = false, isBackPress = false)
        }
    }

    private fun onCustomBackPress() {
        isLivenessSessionFinished = true
        currentCheckBitmap = null
        scope.cancel()
        takeImageTimer?.cancel()
        apiRequestTimer?.cancel()
        finishWithExtra(isTimeoutToManual = false, isBackPress = true)
    }

    private fun enoughTimeForNextCheck(): Boolean {
        return SystemClock.elapsedRealtime() - livenessSessionLimitCheckTime <= LIVENESS_TIME_LIMIT_MILLIS
    }

    private fun setSegmentationFrameSize() {

        try {
            val maskDimens = docData.maskDimensions

            if (maskDimens != null) {
                if (frameSize == null) {
                    val displayMetrics: DisplayMetrics = resources.displayMetrics
                    val densityFactor: Float = displayMetrics.density
                    val dpWidth = displayMetrics.widthPixels / densityFactor

                    val frameWidth = (((dpWidth * (maskDimens.widthPercent / 100)) * densityFactor)
                            * MASK_UI_MULTIPLY_FACTOR).toInt()
                    val frameHeight = (frameWidth * maskDimens.ratio).toInt()

//            Log.d("SEG", "VIEW WIDTH: $dpWidth")
//            Log.d("SEG", "FRAME WIDTH: $frameWidth | FRAME HEIGHT: $frameHeight")

                    frameSize = Size(frameWidth, frameHeight)

                    binding.segmentationFrame.post {
                        binding.segmentationFrame.layoutParams.width = frameSize!!.width
                        binding.segmentationFrame.layoutParams.height = frameSize!!.height
                    }
                    binding.darkFrameOverlay.post {
                        binding.darkFrameOverlay.layoutParams.width = frameSize!!.width - 8
                        binding.darkFrameOverlay.layoutParams.height = frameSize!!.height - 8
                    }
                    binding.stageSuccessFrame.post {
                        binding.stageSuccessFrame.layoutParams.width = frameSize!!.width
                        binding.stageSuccessFrame.layoutParams.height = frameSize!!.height
                    }
                    binding.segmentationMaskWrapper.post {
                        binding.segmentationMaskWrapper.setRectHoleSize(
                            frameSize!!.width - 8, frameSize!!.height - 8)
                    }
                    binding.docAnimationView.post {
                        binding.docAnimationView.layoutParams.width = frameSize!!.width
                        binding.docAnimationView.layoutParams.height = frameSize!!.height
                    }
                }
            } else {
                showSingleToast("Error: cannot retrieve mask dimensions from document type info")
                return
            }
        } catch (e: Error) {
            showSingleToast(e.message)
        } catch (e: Exception) {
            showSingleToast(e.message)
        }

    }

    private suspend fun determineImageResult() {

        blockRequestByProcessing = true

        val fullBitmap: Bitmap = currentCheckBitmap!!
        val minimizedBitmap: Bitmap = fullBitmap.cropWithMask()

        val file = File(createTempFileForBitmapFrame(minimizedBitmap))

        val image: MultipartBody.Part = try {

            val initSizeKb = file.sizeInKb
            if (initSizeKb < 95.0) {
                MultipartBody.Part.createFormData(
                    "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            } else {
                val compressedImageFile = Compressor.compress(this@VCheckSegmentationActivity, file) {
                    destination(file)
                    size(95_000, stepSize = 20, maxIteration = 10)
                }
                MultipartBody.Part.createFormData("image.jpg", compressedImageFile.name,
                    compressedImageFile.asRequestBody("image/jpeg".toMediaType()))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception while compressing Liveness frame image. " +
                    "Attempting to send default frame | \n${e.printStackTrace()}")
            MultipartBody.Part.createFormData(
                "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
        } catch (e: Error) {
            Log.w(TAG, "Error while compressing Liveness frame image. " +
                    "Attempting to send default frame | \n${e.printStackTrace()}")
            MultipartBody.Part.createFormData(
                "image.jpg", file.name, file.asRequestBody("image/jpeg".toMediaType()))
        }

        val response = VCheckDIContainer.mainRepository.sendSegmentationDocAttempt(
                image,
                docData.country,
                docData.category.toString(),
                checkedDocIdx.toString())

        if (response != null) {
            processCheckResult(response, fullBitmap, checkedDocIdx)
        } else {
            blockRequestByProcessing = false
            Log.d(TAG, "Segmentation: response for current index not containing data! Max image size may be exceeded")
        }
    }

    private fun processCheckResult(it: SegmentationGestureResponse,
                                    fullBitmap: Bitmap,
                                    currentPageIdx: Int) {
        runOnUiThread {
            if (it.success) {
                if (currentPageIdx == 0 && photo1FullBitmap == null) {
                    photo1FullBitmap = fullBitmap
                }
                if (currentPageIdx == 1 && photo2FullBitmap == null) {
                    photo2FullBitmap = fullBitmap
                }
                if (!areAllDocPagesChecked() && currentPageIdx == checkedDocIdx) {
                    checkedDocIdx += 1
                    indicateNextStage()
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

        binding.darkFrameOverlay.post {
            binding.darkFrameOverlay.isVisible = false
        }
        binding.stageSuccessFrame.post {
            binding.stageSuccessFrame.isVisible = false
        }

        binding.docAnimationView.post {
            binding.docAnimationView.isVisible = false
        }
        binding.scalableDocHandView.post {
            binding.scalableDocHandView.isVisible = true
        }

        try {
            when(docCategoryIdxToType(docData.category)) {
                DocType.ID_CARD -> {
                    binding.scalableDocHandView.background = AppCompatResources.getDrawable(
                        this@VCheckSegmentationActivity, R.drawable.img_hand_id_card)
                }
                DocType.FOREIGN_PASSPORT -> {
                    binding.scalableDocHandView.background = AppCompatResources.getDrawable(
                        this@VCheckSegmentationActivity, R.drawable.img_hand_foreign_passport)
                } else -> {
                    binding.scalableDocHandView.background = AppCompatResources.getDrawable(
                        this@VCheckSegmentationActivity, R.drawable.img_hand_inner_passport)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Exception while setting hand & doc drawable")
        } catch (e: Error) {
            Log.d(TAG, e.message ?: "Error while setting hand & doc drawable")
        }

        val scaleUp = AnimatorSet()
        val moveDiag = AnimatorSet()
        animateInstructionStage(scaleUp, moveDiag)

        binding.readyButton.setOnClickListener {
            scaleUp.cancel()
            moveDiag.cancel()

            setupDocCheckStage()
        }
    }

    private fun indicateNextStage() {

        vibrateDevice(this@VCheckSegmentationActivity, STAGE_VIBRATION_DURATION_MILLIS)

        binding.segmentationFrame.isVisible = false
        binding.darkFrameOverlay.isVisible = true
        binding.stageSuccessFrame.isVisible = true
        binding.darkFrameOverlay.alpha = 0F
        binding.stageSuccessFrame.alpha = 0F

        binding.tvSegmentationInstruction.setMargins(
            20, 45, 20, 20)

        val isInnerUaPassport = docCategoryIdxToType(docData.category) ==
                DocType.INNER_PASSPORT_OR_COMMON && docData.country == "ua"

        if ((docCategoryIdxToType(docData.category) == DocType.ID_CARD
                    || isInnerUaPassport) && checkedDocIdx == 1) {
            binding.tvSegmentationInstruction.setText(R.string.segmentation_stage_success_first_page)
        } else {
            binding.tvSegmentationInstruction.setText(R.string.segmentation_stage_success)
        }

        fadeDarkOverlayIn()

        binding.docAnimationView.isVisible = (docCategoryIdxToType(docData.category) == DocType.ID_CARD
                || isInnerUaPassport) && checkedDocIdx == 1

        animateStageSuccessFrame()

        if (binding.docAnimationView.isVisible) {
            binding.docAnimationView.cancelAnimation()
            if (checkedDocIdx == 1) {
                if (isInnerUaPassport) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.docAnimationView.scaleX = 2F
                        binding.docAnimationView.scaleY = 2F
                        binding.docAnimationView.setAnimation(R.raw.passport_flip)
                        binding.docAnimationView.playAnimation()
                    }, 900)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.docAnimationView.scaleX = 1F
                        binding.docAnimationView.scaleY = 1F
                        binding.docAnimationView.setAnimation(R.raw.id_card_turn_side)
                        binding.docAnimationView.playAnimation()
                    }, 900)
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed ({
            fadeDarkOverlayOut()
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed ({
            setUIForNextStage()
        }, BLOCK_PIPELINE_TIME_MILLIS)
    }

    private fun setUIForNextStage() {

        binding.segmentationFrame.post {
            binding.segmentationFrame.isVisible = true
        }
        binding.docAnimationView.post {
            binding.docAnimationView.isVisible = false
        }
        binding.darkFrameOverlay.post {
            binding.darkFrameOverlay.isVisible = false
        }
        binding.stageSuccessFrame.post {
            binding.stageSuccessFrame.isVisible = false
        }
        binding.tvSegmentationInstruction.post {
            binding.tvSegmentationInstruction.setMargins(
                20, 45, 20, 20)

            when(docCategoryIdxToType(docData.category)) {
                DocType.FOREIGN_PASSPORT -> {
                    binding.tvSegmentationInstruction.setText(R.string.segmentation_single_page_hint)
                }
                DocType.ID_CARD -> {
                    if (checkedDocIdx == 0) {
                        binding.tvSegmentationInstruction.setText(R.string.segmentation_front_side_hint)
                    }
                    if (checkedDocIdx == 1) {
                        binding.tvSegmentationInstruction.setText(R.string.segmentation_back_side_hint)
                    }
                }
                else -> {
                    if (checkedDocIdx == 0) {
                        binding.tvSegmentationInstruction.setText(R.string.segmentation_front_side_hint)
                    }
                    if (checkedDocIdx == 1) {
                        binding.tvSegmentationInstruction.setText(R.string.segmentation_back_side_hint)
                    }
                }
            }

            blockProcessingByUI = false
            takeImageTimer?.cancel()
            takeImageTimer = null
            setCameraProviderListener()
        }
    }

    private fun animateInstructionStage(scaleUp: AnimatorSet, moveDiag: AnimatorSet) {

        binding.scalableDocHandView.post {
            binding.scalableDocHandView.apply {
                try {
                    val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", 2.6f)
                    val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", 2.6f)
                    scaleUpX.duration = 1000
                    scaleUpY.duration = 1000
                    scaleUpX.repeatCount = ObjectAnimator.INFINITE
                    scaleUpY.repeatCount = ObjectAnimator.INFINITE

                    val moveLeftX: ObjectAnimator = ObjectAnimator.ofFloat(this,
                        "translationX", -280F)
                    val moveUpY: ObjectAnimator = ObjectAnimator.ofFloat(this,
                        "translationY", -160F)
                    moveLeftX.duration = 1000
                    moveUpY.duration = 1000
                    moveLeftX.repeatCount = ObjectAnimator.INFINITE
                    moveUpY.repeatCount = ObjectAnimator.INFINITE

                    scaleUp.play(scaleUpX).with(scaleUpY)
                    moveDiag.play(moveUpY).with(moveLeftX)

                    scaleUp.start()
                    moveDiag.start()
                } catch (e: Exception) {
                    Log.d(TAG, e.message ?: "Exception while trying to animate doc & hand")
                } catch (e: Error) {
                    Log.d(TAG, e.message ?: "Error while setting hand & doc drawable")
                }
            }
        }
    }

    private fun animateStageSuccessFrame() {
        binding.stageSuccessFrame.animate().alpha(1F)
            .setDuration(900)
            .setInterpolator(
            DecelerateInterpolator())
            .withEndAction {
                binding.stageSuccessFrame.animate().alpha(0F)
                    .setDuration(900)
                    .setInterpolator(AccelerateInterpolator()).start()
            }.start()
    }

    private fun fadeDarkOverlayIn() {
        binding.darkFrameOverlay.animate().alpha(1F)
            .setDuration(900)
            .setInterpolator(
                DecelerateInterpolator())
            .start()
    }

    private fun fadeDarkOverlayOut() {
        binding.darkFrameOverlay.animate().alpha(0F)
            .setDuration(900)
            .setInterpolator(
                AccelerateInterpolator())
            .start()
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
        scope.cancel()
        apiRequestTimer?.cancel()
        takeImageTimer?.cancel()
        imageCapture = null
        imageCaptureExecutor.shutdown()
        super.onDestroy()
    }
}
