package com.vcheck.demo.dev.ported

import java.lang.IllegalArgumentException
import kotlin.math.abs

val allMilestoneTypes = arrayOf(
"GestureMilestone",
"OuterHeadAngleMilestone",
"InnerHeadAngleMilestone",
"MouthOpenMilestone",
"MouthClosedMilestone")

data class EulerAngles(val pitch: Double, val yaw: Double, val roll: Double) {

}

open class GestureMilestone(val name: String) {

    open fun isMet(face: Face) : Boolean {
       throw NotImplementedError()
    }
}

open class HeadGestureMilestone(name: String, yawThreshold: Float? = null,
                                pitchThreshold: Float? = null) : GestureMilestone(name) {
    init {
        if (yawThreshold == null && pitchThreshold == null) {
            throw IllegalArgumentException("Either yaw_threshold or pitch_threshold or both are required")
        }
    }
}

class OuterHeadAngleMilestone(val face: Face, name: String, val yawThreshold: Float? = null,
                              val pitchThreshold: Float? = null)
    : HeadGestureMilestone(name, yawThreshold, pitchThreshold) {

    override fun isMet(face: Face): Boolean {
        val eulerAngles = face.eulerAngles()

        return if (yawThreshold != null && pitchThreshold != null) {
            abs(eulerAngles.yaw) > yawThreshold || abs(eulerAngles.pitch) > pitchThreshold
        } else if (yawThreshold != null) {
            abs(eulerAngles.yaw) > yawThreshold
        } else {
            abs(eulerAngles.pitch) > pitchThreshold!! //!
        }
    }
}

class InnerHeadAngleMilestone(val face: Face, name: String, val yawThreshold: Float? = null,
                              val pitchThreshold: Float? = null)
    : HeadGestureMilestone(name, yawThreshold, pitchThreshold) {

    override fun isMet(face: Face): Boolean {
        val eulerAngles = face.eulerAngles()

        return if (yawThreshold != null && pitchThreshold != null) {
            abs(eulerAngles.yaw) < yawThreshold || abs(eulerAngles.pitch) > pitchThreshold
        } else if (yawThreshold != null) {
            abs(eulerAngles.yaw) < yawThreshold
        } else {
            abs(eulerAngles.pitch) < pitchThreshold!! //!
        }
    }
}

class MouthOpenMilestone(val face: Face, name: String, private val ratioThreshold: Float)
    : GestureMilestone(name) {

    override fun isMet(face: Face): Boolean {
        return face.mouthAspectRatio() > ratioThreshold
    }
}

class MouthClosedMilestone(val face: Face, name: String, private val ratioThreshold: Float)
    : GestureMilestone(name) {

    override fun isMet(face: Face): Boolean {
        return face.mouthAspectRatio() < ratioThreshold
    }
}


