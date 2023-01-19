package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.media.MediaFormat
import android.util.Log
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.video.Muxer
import com.vcheck.sdk.core.util.video.MuxerConfig
import com.vcheck.sdk.core.util.video.MuxingCompletionListener
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

fun VCheckLivenessActivity.processLimitedHCFVideoOnResult() {
    setUpLimitedHCFMuxer()

    apiRequestTimer?.cancel()
    takeImageTimer?.cancel()
    fullHCFRecording?.close()
    fullHCFRecording = null
    imageCapture = null
    fullHCFVideoCapture = null
    isLivenessSessionFinished = true

    limitedHCFMuxer!!.setOnMuxingCompletedListener(object : MuxingCompletionListener {
        override fun onVideoSuccessful(file: File) {
            runOnUiThread {
                finishLivenessSession()
                navigateOnLivenessSessionEnd()
            }
        }
        override fun onVideoError(error: Throwable) {
            Log.e(VCheckLivenessActivity.TAG, "There was an error while muxing the video [Limited HCF]: ${error.message}")
            showSingleToast(error.message)
        }
    })

    val finalList = CopyOnWriteArrayList(limitedHCFBitmapList!!)
    Thread { limitedHCFMuxer!!.mux(finalList) }.start()
}

private fun VCheckLivenessActivity.setUpLimitedHCFMuxer() {
    val framesPerImage = 1
    val framesPerSecond = 4F //was 6F
    val bitrate = 2900000
    val muxerConfig = MuxerConfig(createVideoFile() ?: File.createTempFile(
        "faceVideo${System.currentTimeMillis()}", ".mp4", this.cacheDir),
        streamSize!!.height, streamSize!!.width, MediaFormat.MIMETYPE_VIDEO_AVC,
        framesPerImage, framesPerSecond, bitrate, iFrameInterval = 5) //was 1
    limitedHCFMuxer = Muxer(this, muxerConfig)
}