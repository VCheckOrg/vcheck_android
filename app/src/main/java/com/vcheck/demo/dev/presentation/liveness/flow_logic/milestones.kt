package com.vcheck.demo.dev.presentation.liveness.flow_logic

import android.util.Log
import java.lang.IndexOutOfBoundsException

enum class GestureMilestoneType {
    CheckHeadPositionMilestone,
    OuterLeftHeadPitchMilestone,
    OuterRightHeadPitchMilestone,
    InnerHeadPitchMilestone,
    MouthOpenMilestone,
    MouthClosedMilestone
}

const val LEFT_PITCH_PASS_ANGLE = -30.0
const val RIGHT_PITCH_PASS_ANGLE = 30.0
const val MOUTH_OPEN_PASS_FACTOR = 0.41  //reduced from 0.55 !


open class GestureMilestone(val milestoneType: GestureMilestoneType) {

    companion object {
        const val TAG = "MILESTONES"
    }

    //can also add end extend yaw/roll if needed in the future
    open fun isMet(pitchAngle: Double, mouthFactor: Double) : Boolean {
       throw NotImplementedError()
    }
}

class HeadPitchGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    private val leftPitchPassAngle: Double = LEFT_PITCH_PASS_ANGLE
    private val rightPitchPassAngle: Double = RIGHT_PITCH_PASS_ANGLE

    init {
        if (gestureMilestoneType != GestureMilestoneType.InnerHeadPitchMilestone
            && gestureMilestoneType != GestureMilestoneType.OuterLeftHeadPitchMilestone
            && gestureMilestoneType != GestureMilestoneType.OuterRightHeadPitchMilestone
        ) {
            Log.d(TAG, "Head angle milestone type required but not provided!")
        }
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double): Boolean {
        return when(gestureMilestoneType) {
            GestureMilestoneType.InnerHeadPitchMilestone ->
                (pitchAngle < rightPitchPassAngle && pitchAngle > leftPitchPassAngle)
            GestureMilestoneType.OuterLeftHeadPitchMilestone -> pitchAngle < leftPitchPassAngle
            GestureMilestoneType.OuterRightHeadPitchMilestone -> pitchAngle > rightPitchPassAngle
            else -> false
        }
    }
}

class MouthGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    private val mouthOpenPassFactor: Double = MOUTH_OPEN_PASS_FACTOR

    init {
        if (gestureMilestoneType != GestureMilestoneType.MouthClosedMilestone
            && gestureMilestoneType != GestureMilestoneType.MouthOpenMilestone) {
            Log.d(TAG, "Mouth milestone type required but not provided!")
        }
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double): Boolean {
        return when (gestureMilestoneType) {
            GestureMilestoneType.MouthOpenMilestone -> mouthFactor >= mouthOpenPassFactor
            GestureMilestoneType.MouthClosedMilestone -> mouthFactor < mouthOpenPassFactor
            else -> false
        }
    }
}

class CheckOverallHeadPositionMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    private val pitchStableMilestone: HeadPitchGestureMilestone =
        HeadPitchGestureMilestone(GestureMilestoneType.InnerHeadPitchMilestone)
    private val mouthClosedMilestone: MouthGestureMilestone =
        MouthGestureMilestone(GestureMilestoneType.MouthClosedMilestone)

    init {
        if (!areMilestoneTypesMet())
            Log.d(TAG, "CheckOverallHeadPosition: wrong milestone type(s)!")
    }

    private fun areMilestoneTypesMet(): Boolean {
        return pitchStableMilestone.gestureMilestoneType == GestureMilestoneType.InnerHeadPitchMilestone
            && mouthClosedMilestone.gestureMilestoneType == GestureMilestoneType.MouthClosedMilestone
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double): Boolean {
        return if (!areMilestoneTypesMet()) {
            false
        } else {
            pitchStableMilestone.isMet(pitchAngle, mouthFactor) && mouthClosedMilestone.isMet(pitchAngle, mouthFactor)
        }
    }
}

class StandardMilestoneFlow(private val milestoneResultListener: MilestoneResultListener) {

    private val stagesList: List<GestureMilestone> = listOf(
        CheckOverallHeadPositionMilestone(GestureMilestoneType.CheckHeadPositionMilestone),
        HeadPitchGestureMilestone(GestureMilestoneType.OuterLeftHeadPitchMilestone),
        HeadPitchGestureMilestone(GestureMilestoneType.OuterRightHeadPitchMilestone),
        MouthGestureMilestone(GestureMilestoneType.MouthOpenMilestone)
    )

    private var currentStage: Int = 0

    fun checkCurrentStage(pitchAngle: Double, mouthFactor: Double) {
        try {
            if (stagesList[currentStage].isMet(pitchAngle, mouthFactor)) {
                milestoneResultListener.onMilestoneResult(stagesList[currentStage].milestoneType)
                currentStage += 1
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("Liveness", "MILESTONES ERROR: IndexOutOfBoundsException for stages list!")
        }
    }
}

interface MilestoneResultListener {

    fun onMilestoneResult(gestureMilestoneType: GestureMilestoneType)
}

