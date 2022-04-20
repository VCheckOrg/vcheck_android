package com.vcheck.demo.dev.presentation.liveness

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import com.google.mediapipe.framework.TextureFrame
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions
import com.vcheck.demo.dev.R
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import java.lang.IndexOutOfBoundsException
import java.lang.RuntimeException

class LivenessFragment : Fragment() {

    private var facemesh: FaceMesh? = null
    private var cameraInput: CameraInput? = null
    private var glSurfaceView: SolutionGlSurfaceView<FaceMeshResult>? = null

    private var debounceTime: Long = 0

    companion object {
        private const val TAG = "LivenessFragment"
        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
        private const val DEBOUNCE_PROCESS_MILLIS = 600
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_liveness, container, false)
        debounceTime = SystemClock.elapsedRealtime()
        setupStreamingModePipeline(rootView)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        //if (inputSource == InputSource.CAMERA) {
        // Restarts the camera and the opengl surface rendering.
        cameraInput = CameraInput(activity)
        cameraInput!!.setNewFrameListener { textureFrame: TextureFrame? ->
            facemesh!!.send(
                textureFrame
            )
        }
        glSurfaceView!!.post { startCamera() }
        glSurfaceView!!.visibility = View.VISIBLE
        //    } else if (inputSource == InputSource.VIDEO) {
        //      videoInput.resume();
        //    }
    }

    override fun onPause() {
        super.onPause()
        //if (inputSource == InputSource.CAMERA) {
        glSurfaceView!!.visibility = View.GONE
        cameraInput!!.close()
        //}
        //    else if (inputSource == InputSource.VIDEO) {
        //      videoInput.pause();
        //    }
    }

    /** Sets up core workflow for streaming mode.  */
    private fun setupStreamingModePipeline(view: View) {
        Log.i("PIPELINE", "setupStreamingModePipeline START")
        //this.inputSource = inputSource;
        // Initializes a new MediaPipe Face Mesh solution instance in the streaming mode.
        facemesh = FaceMesh(
            activity,
            FaceMeshOptions.builder()
                .setStaticImageMode(false)
                .setRefineLandmarks(true)
                .setRunOnGpu(RUN_ON_GPU)
                .build()
        )
        facemesh!!.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(
                TAG,
                "MediaPipe Face Mesh error:$message"
            )
        }

        //   if (inputSource == InputSource.CAMERA) {
        cameraInput = CameraInput(activity)
        cameraInput!!.setNewFrameListener { textureFrame: TextureFrame? ->
            facemesh!!.send(
                textureFrame
            )
        }
        //    } else if (inputSource == InputSource.VIDEO) {
        //      videoInput = new VideoInput(this);
        //      videoInput.setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
        //    }

        // Initializes a new Gl surface view with a user-defined FaceMeshResultGlRenderer.
        glSurfaceView =
            SolutionGlSurfaceView(activity, facemesh!!.glContext, facemesh!!.glMajorVersion)
        glSurfaceView!!.setSolutionResultRenderer(FaceMeshResultGlRenderer())
        glSurfaceView!!.setRenderInputImage(true)


        facemesh!!.setResultListener { faceMeshResult: FaceMeshResult ->
            //logNoseLandmark(faceMeshResult,  /*showPixelValues=*/false)

            Log.d(TAG, "----  RESULT LISTENER WORKED")
            get2DArrayFromMotionUpdate(faceMeshResult)

            glSurfaceView!!.setRenderData(faceMeshResult)
            glSurfaceView!!.requestRender()
        }

        // The runnable to start camera after the gl surface view is attached.
        // For video input source, videoInput.start() will be called when the video uri is available.
        //if (inputSource == InputSource.CAMERA) {
        glSurfaceView!!.post { startCamera() }
        //}

        // Updates the preview layout.
        val frameLayout = view.findViewById<FrameLayout>(R.id.preview_display_layout)
        //imageView.setVisibility(View.GONE);
        frameLayout.removeAllViewsInLayout()
        frameLayout.addView(glSurfaceView)
        glSurfaceView!!.visibility = View.VISIBLE
        frameLayout.requestLayout()
    }

    private fun startCamera() {
        cameraInput!!.start(
            activity,
            facemesh!!.glContext,
            CameraInput.CameraFacing.FRONT,
            glSurfaceView!!.width,
            glSurfaceView!!.height
        )
    }

    private fun stopCurrentPipeline() {
        if (cameraInput != null) {
            cameraInput!!.setNewFrameListener(null)
            cameraInput!!.close()
        }
        //    if (videoInput != null) {
        //      videoInput.setNewFrameListener(null);
        //      videoInput.close();
        //    }
        if (glSurfaceView != null) {
            glSurfaceView!!.visibility = View.GONE
        }
        if (facemesh != null) {
            facemesh!!.close()
        }
    }

    private fun processLandmarks(faceMeshResult: FaceMeshResult) {
        // convert markers to 2DArray each 1 second (may vary)
        if (SystemClock.elapsedRealtime() - debounceTime >= DEBOUNCE_PROCESS_MILLIS) {
            Log.d(TAG, "----  RESULT LISTENER WORKED WITH DEBOUNCE")
            val convertResult = get2DArrayFromMotionUpdate(faceMeshResult)
            if (convertResult != null) {
                val eulerAnglesResultArr = LandmarkUtil.landmarksToEulerAngles(convertResult)
                Log.d(
                    TAG, "=========== EULER ANGLES " +
                            " | pitch: ${eulerAnglesResultArr[0]}")  // from -30.0 to 30.0 degrees
                //" | yaw: ${eulerAnglesResultArr[1]}" +
                //" | roll: ${eulerAnglesResultArr[2]}")

//                val mouthAspectRatio = LandmarkUtil.landmarksToMouthAspectRatio(convertResult)
//                Log.d(TAG, "========= MOUTH ASPECT RATIO: $mouthAspectRatio")  // >= 055
            }
            debounceTime = SystemClock.elapsedRealtime()
        }
    }

    private fun get2DArrayFromMotionUpdate(result: FaceMeshResult?) : D2Array<Double>? {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return null
        }
        val twoDimArray = mk.d2array(468, 3) { it.toDouble() }

        result.multiFaceLandmarks()[0].landmarkList.forEachIndexed { idx, landmark ->

            val arr = mk.ndarray(doubleArrayOf(
                landmark.x.toDouble(),
                landmark.y.toDouble(),
                (1 - landmark.z).toDouble()))  //was Float typing!

//            Log.i(TAG, "--------------- IDX: $idx")
//            Log.i(TAG, "--------------- x: ${arr[0]} | y: ${arr[1]} | z: ${arr[2]}")

            try {
                if (!arr.isEmpty()) {
                    twoDimArray[idx] = arr
                }
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e.message ?: "2D MARKER ARRAY: caught IndexOutOfBoundsException")
            }
        }
        return twoDimArray
    }




//    private enum class InputSource { //    UNKNOWN,
//        //    IMAGE,
//        //    VIDEO,
//        //    CAMERA,
//    } //private InputSource inputSource = InputSource.CAMERA;

    // Image demo UI and image loader components.
    //private ActivityResultLauncher<Intent> imageGetter;
    //private FaceMeshResultImageView imageView;
    // Video demo UI and video loader components.
    //private VideoInput videoInput;
    //private ActivityResultLauncher<Intent> videoGetter;
    // Live camera demo UI and camera components.
    //  private Bitmap downscaleBitmap(Bitmap originalBitmap) {
    //    double aspectRatio = (double) originalBitmap.getWidth() / originalBitmap.getHeight();
    //    int width = imageView.getWidth();
    //    int height = imageView.getHeight();
    //    if (((double) imageView.getWidth() / imageView.getHeight()) > aspectRatio) {
    //      width = (int) (height * aspectRatio);
    //    } else {
    //      height = (int) (width / aspectRatio);
    //    }
    //    return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    //  }
    //  private Bitmap rotateBitmap(Bitmap inputBitmap, InputStream imageData) throws IOException {
    //    int orientation =
    //            new ExifInterface(imageData)
    //                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    //    if (orientation == ExifInterface.ORIENTATION_NORMAL) {
    //      return inputBitmap;
    //    }
    //    Matrix matrix = new Matrix();
    //    switch (orientation) {
    //      case ExifInterface.ORIENTATION_ROTATE_90:
    //        matrix.postRotate(90);
    //        break;
    //      case ExifInterface.ORIENTATION_ROTATE_180:
    //        matrix.postRotate(180);
    //        break;
    //      case ExifInterface.ORIENTATION_ROTATE_270:
    //        matrix.postRotate(270);
    //        break;
    //      default:
    //        matrix.postRotate(0);
    //    }
    //    return Bitmap.createBitmap(
    //            inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
    //  }
    /** Sets up the UI components for the live demo with camera input.  */ //  private void setupLiveDemoUiComponents() {
    //    Button startCameraButton = findViewById(R.id.button_start_camera);
    //    startCameraButton.setOnClickListener(
    //        v -> {
    //          if (inputSource == InputSource.CAMERA) {
    //            return;
    //          }
    //stopCurrentPipeline();
    //setupStreamingModePipeline(InputSource.CAMERA);
    //});
    //  }
    /** Sets up the UI components for the static image demo.  */ //  private void setupStaticImageDemoUiComponents() {
    // The Intent to access gallery and read images as bitmap.
    //    imageGetter =
    //        registerForActivityResult(
    //            new ActivityResultContracts.StartActivityForResult(),
    //            result -> {
    //              Intent resultIntent = result.getData();
    //              if (resultIntent != null) {
    //                if (result.getResultCode() == RESULT_OK) {
    //                  Bitmap bitmap = null;
    //                  try {
    //                    bitmap =
    //                        downscaleBitmap(
    //                            MediaStore.Images.Media.getBitmap(
    //                                this.getContentResolver(), resultIntent.getData()));
    //                  } catch (IOException e) {
    //                    Log.e(TAG, "Bitmap reading error:" + e);
    //                  }
    //                  try {
    //                    InputStream imageData =
    //                        this.getContentResolver().openInputStream(resultIntent.getData());
    //                    bitmap = rotateBitmap(bitmap, imageData);
    //                  } catch (IOException e) {
    //                    Log.e(TAG, "Bitmap rotation error:" + e);
    //                  }
    //                  if (bitmap != null) {
    //                    facemesh.send(bitmap);
    //                  }
    //                }
    //              }
    //            });
    //    Button loadImageButton = findViewById(R.id.button_load_picture);
    //    loadImageButton.setOnClickListener(
    //        v -> {
    //          if (inputSource != InputSource.IMAGE) {
    //            stopCurrentPipeline();
    //            setupStaticImageModePipeline();
    //          }
    //          // Reads images from gallery.
    //          Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
    //          pickImageIntent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
    //          imageGetter.launch(pickImageIntent);
    //        });
    //imageView = new FaceMeshResultImageView(this);
    //  }
    //  private void setupStaticImageModePipeline() {
    //    this.inputSource = InputSource.IMAGE;
    //    // Initializes a new MediaPipe Face Mesh solution instance in the static image mode.
    //    facemesh =
    //        new FaceMesh(
    //            this,
    //            FaceMeshOptions.builder()
    //                .setStaticImageMode(true)
    //                .setRefineLandmarks(true)
    //                .setRunOnGpu(RUN_ON_GPU)
    //                .build());
    //
    //    // Connects MediaPipe Face Mesh solution to the user-defined FaceMeshResultImageView.
    //    facemesh.setResultListener(
    //        faceMeshResult -> {
    //          logNoseLandmark(faceMeshResult, /*showPixelValues=*/ true);
    //          imageView.setFaceMeshResult(faceMeshResult);
    //          runOnUiThread(() -> imageView.update());
    //        });
    //    facemesh.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Face Mesh error:" + message));
    //
    //    // Updates the preview layout.
    //    FrameLayout frameLayout = findViewById(R.id.preview_display_layout);
    //    frameLayout.removeAllViewsInLayout();
    //    imageView.setImageDrawable(null);
    //    frameLayout.addView(imageView);
    //    imageView.setVisibility(View.VISIBLE);
    //  }
    //
    //  /** Sets up the UI components for the video demo. */
    //  private void setupVideoDemoUiComponents() {
    //    // The Intent to access gallery and read a video file.
    //    videoGetter =
    //        registerForActivityResult(
    //            new ActivityResultContracts.StartActivityForResult(),
    //            result -> {
    //              Intent resultIntent = result.getData();
    //              if (resultIntent != null) {
    //                if (result.getResultCode() == RESULT_OK) {
    //                  glSurfaceView.post(
    //                      () ->
    //                          videoInput.start(
    //                              this,
    //                              resultIntent.getData(),
    //                              facemesh.getGlContext(),
    //                              glSurfaceView.getWidth(),
    //                              glSurfaceView.getHeight()));
    //                }
    //              }
    //            });
    //    Button loadVideoButton = findViewById(R.id.button_load_video);
    //    loadVideoButton.setOnClickListener(
    //        v -> {
    //          stopCurrentPipeline();
    //          setupStreamingModePipeline(InputSource.VIDEO);
    //          // Reads video from gallery.
    //          Intent pickVideoIntent = new Intent(Intent.ACTION_PICK);
    //          pickVideoIntent.setDataAndType(MediaStore.Video.Media.INTERNAL_CONTENT_URI, "video/*");
    //          videoGetter.launch(pickVideoIntent);
    //        });
    //  }
    //    private fun logNoseLandmark(result: FaceMeshResult?, showPixelValues: Boolean) {
    //        if (result == null || result.multiFaceLandmarks().isEmpty()) {
    //            return
    //        }
    //        val noseLandmark = result.multiFaceLandmarks()[0].landmarkList[1]
    //        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
    //        if (showPixelValues) {
    //            val width = result.inputBitmap().width
    //            val height = result.inputBitmap().height
    //            Log.i(
    //                TAG, String.format(
    //                    "MediaPipe Face Mesh nose coordinates (pixel values): x=%f, y=%f",
    //                    noseLandmark.x * width, noseLandmark.y * height
    //                ))
    //        } else {
    //            Log.i(
    //                TAG, String.format(
    //                    "MediaPipe Face Mesh nose normalized coordinates (value range: [0, 1]): x=%f, y=%f",
    //                    noseLandmark.x, noseLandmark.y
    //                ))
    //        }
    //    }
}