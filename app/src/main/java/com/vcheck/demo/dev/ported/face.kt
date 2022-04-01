package com.vcheck.demo.dev.ported

import android.graphics.Rect
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import kotlin.math.*

//import org.opencv.core.Mat

data class FaceError(val exception: Exception)

data class NoFaceError(val error: FaceError)

data class MultipleFacesError(val error: FaceError)

fun normalizeToDimensions(value: Double, dimensions: Double): Double {
    return min(floor(value * dimensions), dimensions - 1)
}

fun faceRectToPoints(image: Any, rect: Rect, scale: Double = 0.8): Pair<Array<Double>, Array<Double>> {
    //h, w = image.shape[:2] //!
    val h: Double = 0.0
    val w: Double = 0.0

    val center: Pair<Double, Double> = Pair(
        normalizeToDimensions(rect.centerX().toDouble(), w),
        normalizeToDimensions(rect.centerY().toDouble(), h))

    val dimensions: Pair<Double, Double> = Pair(
        floor(normalizeToDimensions(rect.width().toDouble(), w) * scale),
        floor(normalizeToDimensions(rect.height().toDouble(), h) * scale))

    return Pair(
        arrayOf((center.first - dimensions.first / 2),
            floor(center.second - dimensions.second / 2)),
        arrayOf(floor(center.first + dimensions.first / 2),
            floor(center.second + dimensions.second / 2)))
}

//fun cutRectangle(image: Mat, p0: Double, p1: Double) {
//    //h, w = image.shape[:2] //!
//    //val h: Double = image.
//    //val w: Double = image.
//
////    return image[
////            max(p0[1], 0) : min(p1[1], h),
////    max(p0[0], 0) : min(p1[0], w),
////    ]
//}


class Face(private val landmarks: NDArray<Float, D3> = mk.d3array(0, 0, 0) { it.toFloat() * it },
           val faceRect: Rect) {

    private var x: MultiArray<Float, D1> = mk.d1array(0) { it.toFloat() * it }

    private var y: MultiArray<Float, D1> = mk.d1array(0) { it.toFloat() * it }

    private var z: MultiArray<Float, D1> = mk.d1array(0) { it.toFloat() * it }

    fun getX(): MultiArray<Float, D1> {
        return landmarks[0][0]
    }

    fun getY(): MultiArray<Float, D1> {
        return landmarks[0][1]
    }

    fun getZ(): MultiArray<Float, D1> {
        return landmarks[0][2]
    }

    fun rotationMatrix(): D2Array<Double> {
        //TODO: implement
        /*
            scaled = (
            self._landmarks
            / np.linalg.norm(self._landmarks[173] - self._landmarks[398])
            ) * 0.06
            centered = scaled - np.mean(scaled, axis=0)

            rotation_matrix = np.empty((3, 3))
            rotation_matrix[0, :] = (centered[356] - centered[127]) / np.linalg.norm(
            centered[356] - centered[127]
            )
            rotation_matrix[1, :] = (centered[152] - centered[8]) / np.linalg.norm(
            centered[152] - centered[8]
            )
            rotation_matrix[2, :] = np.cross(rotation_matrix[0, :], rotation_matrix[1, :])

            return rotation_matrix
         */
        return mk.d2array(0, 0) { it.toDouble() * it }
    }

    fun normalizeToImage(width: Double, height: Double): NDArray<Float, D2> {
        x = x.map { floor(it * width.toFloat()) }
        y = y.map { floor(it * height.toFloat()) }
        z = z.map { floor(it * ((height + width) / 2).toFloat()) }
        x = x.map { if (it > width - 1) (width - 1).toFloat() else it }
        y = y.map { if (it > width - 1) (width - 1).toFloat() else it }
        z = z.map { if (it > (height + width) / 2) ((height + width) / 2).toFloat() else it }

       //return mk.stack(x, y, z, 0) //TODO: find appropriate way to apply stack
        return mk.d2array(0, 0) { it.toFloat() * it } //remove
    }

    fun eulerAngles(): EulerAngles {
        return rotationMatrixToEulerAngles(rotationMatrix()) //TODO adjust return types
    }

    fun mouthAspectRatio(): Double {
        val a = findGCD(landmarks.toFloatArray()[37].toInt(), landmarks.toFloatArray()[83].toInt())
        val b = findGCD(landmarks.toFloatArray()[267].toInt(), landmarks.toFloatArray()[314].toInt())
        val c = findGCD(landmarks.toFloatArray()[61].toInt(), landmarks.toFloatArray()[281].toInt())

        return (a + b) / (2.0 * c)
    }

    fun eyesAspectRatio(): Double {
        val right = (3.0 * findGCD(landmarks.toFloatArray()[33].toInt(),
            landmarks.toFloatArray()[133].toInt())) / (
                findGCD(landmarks.toFloatArray()[160].toInt(),
                        landmarks.toFloatArray()[144].toInt())
                        + findGCD(landmarks.toFloatArray()[159].toInt(),
                            landmarks.toFloatArray()[145].toInt())
                        + findGCD(landmarks.toFloatArray()[158].toInt(),
                            landmarks.toFloatArray()[153].toInt())
                )

        val left = (3.0 * (findGCD(landmarks.toFloatArray()[362].toInt(),
                landmarks.toFloatArray()[263].toInt()))) / (
                findGCD(landmarks.toFloatArray()[384].toInt(),
                    landmarks.toFloatArray()[380].toInt())
                        + findGCD(landmarks.toFloatArray()[385].toInt(), landmarks.toFloatArray()[374].toInt())
                        + findGCD(landmarks.toFloatArray()[386].toInt(), landmarks.toFloatArray()[373].toInt())
                )

        return (right + left) / 2.0
    }


}

fun rotationMatrixToEulerAngles(r: D2Array<Double>): EulerAngles {

    val sy = sqrt(r[0, 0] * r[0, 0] + r[1, 0] * r[1, 0])

    val x = atan2(r[2, 1], r[2, 2])
    val y = atan2(-r[2, 0], sy)
    val z = atan2(r[1, 0], r[0, 0])

    val pitch = -Math.toDegrees(asin(sin(x))) //TODO test if degree value is correct
    val yaw = -Math.toDegrees(asin(sin(y))) //TODO test if degree value is correct
    val roll = -Math.toDegrees(asin(sin(z))) //TODO test if degree value is correct

    //return mk.ndarray(mk[pitch, yaw, roll])
    return EulerAngles(pitch, yaw, roll)
}

fun findGCD(a: Int, b: Int): Int {
    if (a == 0) return b
    return findGCD(b % a, a)
}
