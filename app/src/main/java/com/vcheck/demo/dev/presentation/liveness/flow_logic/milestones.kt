package com.vcheck.demo.dev.presentation.liveness.flow_logic

import android.util.Log
import java.lang.IndexOutOfBoundsException
import kotlin.math.abs

enum class GestureMilestoneType {

    CheckHeadPositionMilestone,

    OuterLeftHeadYawMilestone,
    OuterRightHeadYawMilestone,

    InnerHeadPitchMilestone,
    UpHeadPitchMilestone,
    DownHeadPitchMilestone,

    MouthOpenMilestone,
    MouthClosedMilestone
}

enum class ObstacleType {
    PITCH_ANGLE,
    MULTIPLE_FACES_DETECTED,
    NO_OR_PARTIAL_FACE_DETECTED,
}

const val PITCH_STRAIGHT_CHECK_ANGLE_ABS = 20.0
const val PITCH_UP_PASS_ANGLE = 20.0
const val PITCH_DOWN_PASS_ANGLE = -20.0

const val LEFT_YAW_PASS_ANGLE = -30.0
const val RIGHT_YAW_PASS_ANGLE = 30.0
const val MOUTH_OPEN_PASS_FACTOR = 0.35  //reduced from 0.55 !


open class GestureMilestone(val milestoneType: GestureMilestoneType) {

    companion object {
        const val TAG = "MILESTONES"
    }

    //can also add end extend yaw/roll if needed in the future
    open fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double) : Boolean {
       throw NotImplementedError()
    }
}

class HeadPitchGestureMilestone(val gestureMilestoneType: GestureMilestoneType) : GestureMilestone(gestureMilestoneType) {

    init {
        if (gestureMilestoneType != GestureMilestoneType.UpHeadPitchMilestone
            && gestureMilestoneType != GestureMilestoneType.DownHeadPitchMilestone) {
            Log.d(TAG, "Head angle milestone type required but not provided!")
        }
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {

        return when(gestureMilestoneType) {
            GestureMilestoneType.DownHeadPitchMilestone ->
                pitchAngle < PITCH_DOWN_PASS_ANGLE
            GestureMilestoneType.UpHeadPitchMilestone ->
                pitchAngle > PITCH_UP_PASS_ANGLE
            else -> false
        }
    }
}

class HeadYawGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    init {
        if (gestureMilestoneType != GestureMilestoneType.OuterLeftHeadYawMilestone
            && gestureMilestoneType != GestureMilestoneType.OuterRightHeadYawMilestone) {
            Log.d(TAG, "Head angle milestone type required but not provided!")
        }
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
        return when(gestureMilestoneType) {
            GestureMilestoneType.OuterLeftHeadYawMilestone ->
                yawAngle < LEFT_YAW_PASS_ANGLE && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
            GestureMilestoneType.OuterRightHeadYawMilestone ->
                yawAngle > RIGHT_YAW_PASS_ANGLE && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
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

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
        return when (gestureMilestoneType) {
            GestureMilestoneType.MouthOpenMilestone ->
                mouthFactor >= MOUTH_OPEN_PASS_FACTOR && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
            GestureMilestoneType.MouthClosedMilestone ->
                mouthFactor < MOUTH_OPEN_PASS_FACTOR && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
            else -> false
        }
    }
}

class CheckOverallHeadPositionMilestone(val gestureMilestoneType: GestureMilestoneType)
    : GestureMilestone(gestureMilestoneType) {

    //TODO remove/simplify on iOS!
//    private val pitchStableMilestone: HeadPitchGestureMilestone =
//        HeadPitchGestureMilestone(GestureMilestoneType.CheckHeadPositionMilestone)
    private val mouthClosedMilestone: MouthGestureMilestone =
        MouthGestureMilestone(GestureMilestoneType.MouthClosedMilestone)

    init {
        if (!areMilestoneTypesMet())
            Log.d(TAG, "CheckOverallHeadPosition: wrong milestone type(s)!")
    }

    private fun areMilestoneTypesMet(): Boolean {
        //return pitchStableMilestone.gestureMilestoneType == GestureMilestoneType.InnerHeadPitchMilestone &&
        return mouthClosedMilestone.gestureMilestoneType == GestureMilestoneType.MouthClosedMilestone
    }

    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
        return if (!areMilestoneTypesMet()) {
            false
        } else {
            //pitchStableMilestone.isMet(pitchAngle, mouthFactor, yawAngle) &&
            mouthClosedMilestone.isMet(pitchAngle, mouthFactor, yawAngle)
        }
    }
}

class StandardMilestoneFlow(private val milestoneResultListener: MilestoneResultListener) {

    private var stagesList: List<GestureMilestone> = listOf(
        CheckOverallHeadPositionMilestone(GestureMilestoneType.CheckHeadPositionMilestone)
    )

    private var currentStageIdx: Int = 0

    fun resetStages() {
        currentStageIdx = 0
    }

    fun setStagesList(list: List<String>) {
        val gestures: List<GestureMilestone> = list.map {
            gmFromServiceValue(it)
        }
        stagesList = stagesList + gestures
    }

    fun getCurrentStage(): GestureMilestone {
        return if (currentStageIdx > (stagesList.size - 1)) {
            stagesList[0]
        } else {
            stagesList[currentStageIdx]
        }
    }

    fun checkCurrentStage(pitchAngle: Double, mouthFactor: Double, yawAbsAngle: Double) {
        try {
            Log.d("Liveness", "STAGES LIST: $stagesList")
            Log.d("Liveness", "CURRENT STAGE TYPE: ${stagesList[currentStageIdx].milestoneType}")
            Log.d("Liveness", "CURRENT STAGE IDX: $currentStageIdx")
            if (currentStageIdx > (stagesList.size - 1)) {
                milestoneResultListener.onAllStagesPassed()
                return
            } else if (stagesList[currentStageIdx].isMet(pitchAngle, mouthFactor, yawAbsAngle)) {
                currentStageIdx += 1 //!
                milestoneResultListener.onMilestoneResult(stagesList[currentStageIdx].milestoneType)
                return
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("Liveness", "MILESTONES ERROR: IndexOutOfBoundsException for stages list!")
        }
    }

    private fun gmFromServiceValue(strValue: String): GestureMilestone {
        return when(strValue) {
            "left" -> HeadYawGestureMilestone(GestureMilestoneType.OuterLeftHeadYawMilestone)
            "right" ->HeadYawGestureMilestone(GestureMilestoneType.OuterRightHeadYawMilestone)
            "up" ->HeadPitchGestureMilestone(GestureMilestoneType.UpHeadPitchMilestone)
            "down" -> HeadPitchGestureMilestone(GestureMilestoneType.DownHeadPitchMilestone)
            "mouth" -> MouthGestureMilestone(GestureMilestoneType.MouthOpenMilestone)
            else -> CheckOverallHeadPositionMilestone(GestureMilestoneType.CheckHeadPositionMilestone)
        }
    }
}



interface MilestoneResultListener {

    fun onMilestoneResult(gestureMilestoneType: GestureMilestoneType)

    fun onObstacleMet(obstacleType: ObstacleType)

    fun onAllStagesPassed()
}



//    fun getUndoneStage(): GestureMilestone {
//        return if (currentStageIdx == 0) stagesList[0] else stagesList[currentStageIdx - 1]
//    }


//            if (yawAbsAngle > PITCH_STRAIGHT_CHECK_ANGLE_ABS) {
//                milestoneResultListener.onObstacleMet(ObstacleType.PITCH_ANGLE)
//            } else {

//CheckOverallHeadPositionMilestone(GestureMilestoneType.CheckHeadPositionMilestone),
//HeadYawGestureMilestone(GestureMilestoneType.OuterLeftHeadPitchMilestone),
//HeadYawGestureMilestone(GestureMilestoneType.OuterRightHeadPitchMilestone),
//MouthGestureMilestone(GestureMilestoneType.MouthOpenMilestone)