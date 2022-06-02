package com.vcheck.demo.dev.presentation.liveness.ui.failures

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.presentation.StartupActivity
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class LivenessResultVideoViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liveness_result_video_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            //Stub; not going back from here
        }

        val videoPath = (activity as LivenessActivity).videoPath

        val videoView: VideoView = view.findViewById(R.id.videoView)

        if (videoPath != null) {
            val video: Uri = videoPath.toUri()
            videoView.setVideoURI(video)
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                videoView.start()
            }

            saveVideoTOGalleryForChecking(videoPath, "VcheckVideo${System.currentTimeMillis()}")
        }
    }

    private fun saveVideoTOGalleryForChecking(filePath: String, fileName: String) {
        val context = (activity as LivenessActivity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filePath.let {

                val values = ContentValues().apply {
                    val folderName = Environment.DIRECTORY_MOVIES

                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")

                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        folderName + "/${context.getString(R.string.app_name)}/")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                val fileUri = context.contentResolver.insert(collection, values)

                fileUri?.let {
                    context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                        descriptor?.let {
                            FileOutputStream(descriptor.fileDescriptor).use { out ->
                                val videoFile = File(filePath)
                                FileInputStream(videoFile).use { inputStream ->
                                    val buf = ByteArray(8192)
                                    while (true) {
                                        val sz = inputStream.read(buf)
                                        if (sz <= 0) break
                                        out.write(buf, 0, sz)
                                    }
                                }
                            }
                        }
                        values.clear()
                        values.put(MediaStore.Video.Media.IS_PENDING, 0)
                        context.contentResolver.update(fileUri, values, null, null)
                    }
                }
            }
        } else {
            //Stub
        }
    }

    private fun resetApplication() {
        val resetApplicationIntent = (activity as StartupActivity).applicationContext
            .packageManager.getLaunchIntentForPackage(
                (activity as StartupActivity).applicationContext.packageName)

        if (resetApplicationIntent != null) {
            resetApplicationIntent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        (activity as LivenessActivity).startActivity(resetApplicationIntent)
        (context as LivenessActivity).overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }


}