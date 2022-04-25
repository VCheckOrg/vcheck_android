package com.vcheck.demo.dev.presentation.liveness.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import java.io.File


class LivenessResultVideoViewFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liveness_result_video_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoPath = (activity as LivenessActivity).videoPath

        val videoView: VideoView = view.findViewById(R.id.videoView)

        if (videoPath != null) {
            val video: Uri = videoPath.toUri()
            videoView.setVideoURI(video)
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                videoView.start()
            }

            Log.d("mux", getFolderSizeLabel(File(videoPath)))
            //saveVideoTOGallery(videoPath, "VcheckVideo")
        }
    }

    private fun getFolderSizeLabel(file: File): String {
        val size = getFolderSize(file).toDouble() / 1000.0 // Get size and convert bytes into KB.
        return if (size >= 1024) {
            (size / 1024).toString() + " MB"
        } else {
            "$size KB"
        }
    }

    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        if (file.isDirectory) {
            for (child in file.listFiles()) {
                size += getFolderSize(child)
            }
        } else {
            size = file.length()
        }
        return size
    }

//    private fun saveVideoTOGalleryForChecking(filePath: String?, fileName: String) {
//        filePath?.let {
//            val context = (activity as LivenessActivity)
//            val values = ContentValues().apply {
//                val folderName = Environment.DIRECTORY_MOVIES
//
//                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//                put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    put(MediaStore.Images.Media.RELATIVE_PATH,
//                        folderName + "/${context.getString(R.string.app_name)}/")
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    put(MediaStore.Images.Media.IS_PENDING, 1)
//                }
//            }
//
//            val collection =
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//                } else {
//                    TODO("VERSION.SDK_INT < Q")
//                }
//
//            val fileUri = context.contentResolver.insert(collection, values)
//
//            fileUri?.let {
//                context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
//                    descriptor?.let {
//                        FileOutputStream(descriptor.fileDescriptor).use { out ->
//                            val videoFile = File(filePath)
//                            FileInputStream(videoFile).use { inputStream ->
//                                val buf = ByteArray(8192)
//                                while (true) {
//                                    val sz = inputStream.read(buf)
//                                    if (sz <= 0) break
//                                    out.write(buf, 0, sz)
//                                }
//                            }
//                        }
//                    }
//                    values.clear()
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        values.put(MediaStore.Video.Media.IS_PENDING, 0)
//                    }
//                    context.contentResolver.update(fileUri, values, null, null)
//                }
//            }
//        }
//    }

}