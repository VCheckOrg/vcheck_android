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

enum class ObstacleType {
    YAW_ANGLE,
    WRONG_GESTURE,
    MULTIPLE_FACES_DETECTED,
    NO_STRAIGHT_FACE_DETECTED,
    BRIGHTNESS_LEVEL_IS_LOW,
    MOTIONS_ARE_TOO_SHARP
}

const val YAW_PASS_ANGLE_ABS = 20.0
const val LEFT_PITCH_PASS_ANGLE = -30.0
const val RIGHT_PITCH_PASS_ANGLE = 30.0
const val MOUTH_OPEN_PASS_FACTOR = 0.39  //reduced from 0.55 !

const val NEXT_FRAME_MAX_PITCH_DIFF = 15.0 //!
const val NEXT_FRAME_MAX_YAW_DIFF = 8.0


open class GestureMilestone(val milestoneType: GestureMilestoneType) {

    companion object {
        const val TAG = "MILESTONES"
    }

    //can also add end extend yaw/roll if needed in the future
    open fun isMet(pitchAngle: Double, mouthFactor: Double, yawAbsAngle: Double) : Boolean {
       throw NotImplementedError()
    }
}

class HeadPitchGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    init {
        if (gestureMilestoneType != GestureMilestoneType.InnerHeadPitchMilestone
            && gestureMilestoneType != GestureMilestoneType.OuterLeftHeadPitchMilestone
            && gestureMilestoneType != GestureMilestoneType.OuterRightHeadPitchMilestone
        ) {
            Log.d(TAG, "Head angle milestone type required but not provided!")
        }
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAbsAngle: Double): Boolean {
        return when(gestureMilestoneType) {
            GestureMilestoneType.InnerHeadPitchMilestone ->
                (pitchAngle < RIGHT_PITCH_PASS_ANGLE
                        && pitchAngle > LEFT_PITCH_PASS_ANGLE
                        && yawAbsAngle < YAW_PASS_ANGLE_ABS)
            GestureMilestoneType.OuterLeftHeadPitchMilestone ->
                pitchAngle < LEFT_PITCH_PASS_ANGLE && yawAbsAngle < YAW_PASS_ANGLE_ABS
            GestureMilestoneType.OuterRightHeadPitchMilestone ->
                pitchAngle > RIGHT_PITCH_PASS_ANGLE && yawAbsAngle < YAW_PASS_ANGLE_ABS
            else -> false
        }
    }
}

class MouthGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    init {
        if (gestureMilestoneType != GestureMilestoneType.MouthClosedMilestone
            && gestureMilestoneType != GestureMilestoneType.MouthOpenMilestone) {
            Log.d(TAG, "Mouth milestone type required but not provided!")
        }
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAbsAngle: Double): Boolean {
        return when (gestureMilestoneType) {
            GestureMilestoneType.MouthOpenMilestone ->
                mouthFactor >= MOUTH_OPEN_PASS_FACTOR && yawAbsAngle < YAW_PASS_ANGLE_ABS
            GestureMilestoneType.MouthClosedMilestone ->
                mouthFactor < MOUTH_OPEN_PASS_FACTOR && yawAbsAngle < YAW_PASS_ANGLE_ABS
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

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAbsAngle: Double): Boolean {
        return if (!areMilestoneTypesMet()) {
            false
        } else {
            pitchStableMilestone.isMet(pitchAngle, mouthFactor, yawAbsAngle)
                    && mouthClosedMilestone.isMet(pitchAngle, mouthFactor, yawAbsAngle)
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

    private var currentStageIdx: Int = 0

    fun getUndoneStage(): GestureMilestone {
        return if (currentStageIdx == 0) stagesList[0] else stagesList[currentStageIdx - 1]
    }

    fun getCurrentStage(): GestureMilestone {
        return stagesList[currentStageIdx]
    }

    fun checkCurrentStage(pitchAngle: Double, mouthFactor: Double, yawAbsAngle: Double) {
        try {
            if (yawAbsAngle > YAW_PASS_ANGLE_ABS) {
                milestoneResultListener.onObstacleMet(ObstacleType.YAW_ANGLE)
            } else {
                if (stagesList[currentStageIdx].isMet(pitchAngle, mouthFactor, yawAbsAngle)) {
                    milestoneResultListener.onMilestoneResult(stagesList[currentStageIdx].milestoneType)
                    currentStageIdx += 1
                } else {
                    stagesList.forEachIndexed { idx, stage ->
                        if (idx != 0 && idx != currentStageIdx
                            && stage.isMet(pitchAngle, mouthFactor, yawAbsAngle)) {
                            milestoneResultListener.onObstacleMet(ObstacleType.WRONG_GESTURE)
                        }
                    }
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("Liveness", "MILESTONES ERROR: IndexOutOfBoundsException for stages list!")
        }
    }

//    func hasExtensivePitchDiff(pitchAngle: Float) -> Bool {
//        if ((pitchAngle < 0 && recentFramePitchAngle < 0) || (pitchAngle > 0 && recentFramePitchAngle > 0)) {
////            let diff = abs(abs(pitchAngle) - abs(recentFramePitchAngle))
////            print("PITCH DIFF: \(diff)")
//            return abs(abs(pitchAngle) - abs(recentFramePitchAngle)) > MilestoneConstraints.NEXT_FRAME_MAX_PITCH_DIFF
//        } else if (pitchAngle != 0.0 && recentFrameYawAngle != 0.0) {
////            let diff = (abs(pitchAngle) + abs(recentFramePitchAngle))
////            print("PITCH DIFF: \(diff)")
//            return (abs(pitchAngle) + abs(recentFramePitchAngle)) > MilestoneConstraints.NEXT_FRAME_MAX_PITCH_DIFF
//        } else {
//            return false
//        }
//    }
//
//    func hasExtensiveYawDiff(yawAngle: Float) -> Bool {
//        if ((yawAngle < 0 && recentFrameYawAngle < 0) || (yawAngle > 0 && recentFrameYawAngle > 0)) {
////            let diff = abs(abs(yawAngle) - abs(recentFrameYawAngle))
////            print("YAW DIFF: \(diff)")
//            return abs(abs(yawAngle) - abs(recentFrameYawAngle)) > MilestoneConstraints.NEXT_FRAME_MAX_PITCH_DIFF
//        } else if (yawAngle != 0.0 && recentFrameYawAngle != 0.0) {
////            let diff = (abs(yawAngle) + abs(recentFrameYawAngle))
////            print("YAW DIFF: \(diff)")
//            return (abs(yawAngle) + abs(recentFrameYawAngle)) > MilestoneConstraints.NEXT_FRAME_MAX_YAW_DIFF
//        } else {
//            return false
//        }
//    }
}

interface MilestoneResultListener {

    fun onMilestoneResult(gestureMilestoneType: GestureMilestoneType)

    fun onObstacleMet(obstacleType: ObstacleType)
}

