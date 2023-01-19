package com.vcheck.sdk.core.presentation.liveness.flow_logic

import android.util.Log
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity

fun VCheckLivenessActivity.buildVideoCaptureUseCase() {

    val selector = QualitySelector
        .from(
            Quality.FHD,
            FallbackStrategy.higherQualityOrLowerThan(Quality.SD))

    val recorder = Recorder.Builder()
        .setQualitySelector(selector)
        .build()

    // Create the VideoCapture UseCase and make it available to use
    // in the other part of the application.
    fullHCFVideoCapture = VideoCapture.withOutput(recorder)
}

fun VCheckLivenessActivity.setupVideoRecording() {
    val recordingListener = Consumer<VideoRecordEvent> { event ->
        when(event) {
            is VideoRecordEvent.Start -> {
                Log.d(VCheckLivenessActivity.TAG,"Capture Started")
            }
            is VideoRecordEvent.Finalize -> {
                if (!event.hasError()) {
                    // finish liveness session and navigate forward for video upload
                    videoPath = event.outputResults.outputUri.path

                    finishLivenessSession()
                    navigateOnLivenessSessionEnd()

                    Log.d(VCheckLivenessActivity.TAG, "Video capture succeeded: ${event.outputResults.outputUri}")
                } else {
                    fullHCFRecording?.close()
                    fullHCFRecording = null
                    Log.e(VCheckLivenessActivity.TAG, "Video capture ends with error [Full HCF]: ${event.error}")
                }
            }
        }
    }

    val outputOptions = FileOutputOptions.Builder(createVideoFile()!!).build()

    fullHCFRecording = (fullHCFVideoCapture?.output as Recorder)
        .prepareRecording(this, outputOptions)
        .start(ContextCompat.getMainExecutor(this), recordingListener)
}

fun VCheckLivenessActivity.processFullHCFVideoOnResult() {
    fullHCFRecording?.stop()
}