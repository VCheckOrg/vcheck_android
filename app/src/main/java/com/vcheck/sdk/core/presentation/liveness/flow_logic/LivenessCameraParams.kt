package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.graphics.Bitmap

data class LivenessCameraParams(
    var previewHeight: Int = 0,
    var previewWidth: Int = 0,
    var sensorOrientation: Int = 0,
    var isProcessingFrame: Boolean = false,
    val yuvBytes: Array<ByteArray?> = arrayOfNulls(3),
    var rgbBytes: IntArray? = null,
    var yRowStride: Int = 0,
    var postInferenceCallback: Runnable? = null,
    var imageConverter: Runnable? = null,
    var rgbFrameBitmap: Bitmap? = null
)