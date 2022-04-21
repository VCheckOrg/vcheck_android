package com.vcheck.demo.dev.presentation.liveness.flow_logic

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import org.jetbrains.kotlinx.multik.api.mk

import kotlin.math.*

object LandmarksProcessingUtil {

    fun landmarksToEulerAngles(landmarks: D2Array<Double>): D1Array<Double> {

        val landmarksCount = landmarks.shape[0]
        val scaled = (landmarks / normDouble(landmarks[173..174] - landmarks[398..399])) * 0.06
        val mean = mean<Double, D2, D1>(scaled, axis=0)
        val centered = scaled - mean.repeat(landmarksCount).reshape(landmarksCount, 3)

        val a = (centered[356..357] - centered[127..128]) / normDouble(centered[356..357] - centered[127..128])
        val b = (centered[152..153] - centered[8..9]) / normDouble(centered[152..153] - centered[8..9])
        val c = crossProd(a[0], b[0])

        val r = mk.stack(a[0], b[0], c)
        val sy = sqrt(r[0, 0] * r[0, 0] + r[1, 0] * r[1, 0])

        val x = atan2(r[2, 1], r[2, 2])
        val y = atan2(-r[2, 0], sy)
        val z = atan2(r[1, 0], r[0, 0])

        val pitch = -toDegrees(asin(sin(x)))
        val yaw = -toDegrees(asin(sin(y)))
        val roll = -toDegrees(asin(sin(z)))

        return mk.ndarray(mk[pitch, yaw, roll])
    }

    fun landmarksToMouthAspectRatio(landmarks: D2Array<Double>): Double {
        val a = euclidean(landmarks[37], landmarks[83])
        val b = euclidean(landmarks[267], landmarks[314])
        val c = euclidean(landmarks[61], landmarks[281])

        return (a + b / (2.0 * c))
    }


    private fun euclidean(p0: MultiArray<Double, D1>, p1:  MultiArray<Double, D1>): Double {
        return sqrt((p0[0] - p1[0]).pow(2) + (p0[1] - p1[1]).pow(2) + (p0[2] - p1[2]).pow(2))
    }

    private fun crossProd(a: MultiArray<Double, D1>, b: MultiArray<Double, D1>): MultiArray<Double, D1> {
        return mk.ndarray(mk[a[1]*b[2] - a[2]*b[1], a[2]*b[0] - a[0]*b[2], a[0]*b[1] - a[1]*b[0]])
    }

    private fun toDegrees (i: Double): Double {
        return  i * 180 / PI
    }


    private fun normDouble(mat: MultiArray<Double, D2>, p: Int = 2): Double {
        require(p > 0) { "Power $p must be positive" }

        return norm(mat.data.getDoubleArray(), mat.offset, mat.strides, mat.shape[0], mat.shape[1], p, mat.consistent)
    }

    private fun norm(mat: DoubleArray, matOffset: Int, matStrides: IntArray,
                      n: Int, m: Int, power: Int, consistent: Boolean): Double {
        //most common case of matrix elements
        var result = 0.0

        val (matStride_0, matStride_1) = matStrides

        if (consistent) {
            result = mat.sumOf { abs(it).pow(power) }
        } else {
            for (i in 0 until n) {
                val matInd = i * matStride_0 + matOffset
                for (k in 0 until m) {
                    val absValue = abs(mat[matInd + k * matStride_1])
                    result += absValue.pow(power)
                }
            }
        }
        return result.pow(1.0 / power)
    }

    private fun <T : Number, D : Dimension, O : Dimension> mean(a: MultiArray<T, D>, axis: Int = 0): NDArray<Double, O> {
        require(a.dim.d > 1) { "NDArray of dimension one, use the `mean` function without axis." }
        require(axis in 0 until a.dim.d) { "axis $axis is out of bounds for this ndarray of dimension ${a.dim.d}." }
        val newShape = a.shape.remove(axis)
        val retData = initMemoryView<Double>(newShape.fold(1, Int::times), DataType.DoubleDataType)
        val indexMap: MutableMap<Int, Indexing> = mutableMapOf()
        for (i in a.shape.indices) {
            if (i == axis) continue
            indexMap[i] = 0.r..a.shape[i]
        }
        for (index in 0 until a.shape[axis]) {
            indexMap[axis] = index.r
            val t = a.slice<T, D, O>(indexMap)
            var count = 0
            for (element in t) {
                retData[count++] += element.toDouble()
            }
        }

        return NDArray<Double, O>(
            retData, 0, newShape, dim = dimensionOf(newShape.size)
        ) / a.shape[axis].toDouble()
    }

    private fun IntArray.remove(pos: Int): IntArray = when (pos) {
        0 -> sliceArray(1..lastIndex)
        lastIndex -> sliceArray(0 until lastIndex)
        else -> sliceArray(0 until pos) + sliceArray(pos + 1..lastIndex)
    }

// Obsolete:
//        return when (mat.dtype) {
//            DataType.DoubleDataType -> {
//            }
//            DataType.FloatDataType -> {
//                norm(mat.data.getDoubleArray(), mat.offset, mat.strides, mat.shape[0], mat.shape[1], p, mat.consistent)
//            }
//            else -> {
//                norm(mat.data, mat.offset, mat.strides, mat.shape[0], mat.shape[1], p, mat.consistent)
//            }
//        }

}

