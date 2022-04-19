package com.vcheck.demo.dev.ported

import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*

import kotlin.math.*


fun landmarksToEulerAngles(landmarks: D2Array<Double>): D1Array<Double> {
    val landmarksCount = landmarks.shape[0]
    val scaled = (landmarks / mk.linalg.norm(landmarks[173..174] - landmarks[398..399])) * 0.06
    val mean = mk.stat.meanD2(scaled, axis=0)
    val centered = scaled - mean.repeat(landmarksCount).reshape(landmarksCount, 3)

    val a = (centered[356..357] - centered[127..128]) / mk.linalg.norm(centered[356..357] - centered[127..128])
    val b = (centered[152..153] - centered[8..9]) / mk.linalg.norm(centered[152..153] - centered[8..9])
    val c = crossProd(a[0], b[0])

    val R = mk.stack(a[0], b[0], c)
    val sy = sqrt(R[0, 0] * R[0, 0] + R[1, 0] * R[1, 0])

    val x = atan2(R[2, 1], R[2, 2])
    val y = atan2(-R[2, 0], sy)
    val z = atan2(R[1, 0], R[0, 0])

    val pitch = -toDegrees(asin(sin(x)))
    val yaw = -toDegrees(asin(sin(y)))
    val roll = -toDegrees(asin(sin(z)))

    return mk.ndarray(mk[pitch, yaw, roll])
}

fun landmarksToMouthAspectRatio(landmarks: D2Array<Double>): Double {
    val A = euclidean(landmarks[37], landmarks[83])
    val B = euclidean(landmarks[267], landmarks[314])
    val C = euclidean(landmarks[61], landmarks[281])

    return (A + B / (2.0 * C))
}


fun euclidean(p0: MultiArray<Double, D1>, p1:  MultiArray<Double, D1>): Double {
    return sqrt((p0[0] - p1[0]).pow(2) + (p0[1] - p1[1]).pow(2) + (p0[2] - p1[2]).pow(2))
}

fun crossProd(a: MultiArray<Double, D1>, b: MultiArray<Double, D1>): MultiArray<Double, D1> {
    return mk.ndarray(mk[a[1]*b[2] - a[2]*b[1], a[2]*b[0] - a[0]*b[2], a[0]*b[1] - a[1]*b[0]])
}

fun toDegrees (i: Double): Double {
    return i * 180 / PI
}
