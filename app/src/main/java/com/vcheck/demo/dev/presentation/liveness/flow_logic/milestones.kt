package com.vcheck.demo.dev.presentation.liveness.flow_logic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import java.lang.IndexOutOfBoundsException
import kotlin.math.abs

enum class GestureMilestoneType {

    //CheckHeadPositionMilestone,
    //MouthClosedMilestone

    OuterLeftHeadYawMilestone,
    OuterRightHeadYawMilestone,

    UpHeadPitchMilestone,
    DownHeadPitchMilestone,

    MouthOpenMilestone
}

class StandardMilestoneFlow() {

    private var stagesList = emptyList<GestureMilestoneType>()

    private var currentStageIdx: Int = 0

    fun resetStages() {
        currentStageIdx = 0
    }

    fun setStagesList(list: List<String>) {
        val gestures: List<GestureMilestoneType> = list.map {
            gmFromServiceValue(it)!!
        }
        stagesList = gestures
    }

    fun getCurrentStage(): GestureMilestoneType {
        return if (currentStageIdx > (stagesList.size - 1)) {
            stagesList[0]
        } else {
            stagesList[currentStageIdx]
        }
    }

    fun areAllStagesPassed(): Boolean {
        return currentStageIdx > (stagesList.size - 1)
    }

    fun incrementCurrentStage() {
        currentStageIdx += 1
    }

    private fun gmFromServiceValue(strValue: String): GestureMilestoneType? {
        return when(strValue) {
            "left" -> GestureMilestoneType.OuterLeftHeadYawMilestone
            "right" -> GestureMilestoneType.OuterRightHeadYawMilestone
            "up" -> GestureMilestoneType.UpHeadPitchMilestone
            "down" -> GestureMilestoneType.DownHeadPitchMilestone
            "mouth" -> GestureMilestoneType.MouthOpenMilestone
            else -> null
        }
    }

    fun getGestureRequestFromCurrentStage(): String {
        return try {
            when(stagesList[currentStageIdx]) {
                GestureMilestoneType.OuterLeftHeadYawMilestone -> "left"
                GestureMilestoneType.OuterRightHeadYawMilestone -> "right"
                GestureMilestoneType.UpHeadPitchMilestone -> "up"
                GestureMilestoneType.DownHeadPitchMilestone -> "down"
                GestureMilestoneType.MouthOpenMilestone-> "mouth"
            }
        } catch (e: IndexOutOfBoundsException) {
            ""
        }
    }
}



//interface MilestoneResultListener {
//
//    fun onMilestoneResult(gestureMilestoneType: GestureMilestoneType)
//
//    fun onObstacleMet(obstacleType: ObstacleType)
//
//    fun onAllStagesPassed()
//}



//    fun getCurrentStage(): GestureMilestone {
//        return if (currentStageIdx > (stagesList.size - 1)) {
//            stagesList[0]
//        } else {
//            stagesList[currentStageIdx]
//        }
//    }

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

//            Log.d("Liveness", "STAGES LIST: $stagesList")
//            Log.d("Liveness", "CURRENT STAGE TYPE: ${stagesList[currentStageIdx].milestoneType}")
//            Log.d("Liveness", "CURRENT STAGE IDX: $currentStageIdx")
//            if (stagesList[currentStageIdx].isMet(pitchAngle, mouthFactor, yawAbsAngle)) {
//                try {
//                    currentStageIdx += 1
//                    if (currentStageIdx > (stagesList.size - 1)) {
//                        milestoneResultListener.onAllStagesPassed()
//                    } else {
//                        milestoneResultListener.onMilestoneResult(stagesList[currentStageIdx].milestoneType)
//                    }
//                    return
//                } catch (e: IndexOutOfBoundsException) {
//                    Log.d("Liveness", "MILESTONES ERROR: IndexOutOfBoundsException for stages list!")
//                }
//            }


//class HeadPitchGestureMilestone(val gestureMilestoneType: GestureMilestoneType) : GestureMilestone(gestureMilestoneType) {
//
//    init {
//        if (gestureMilestoneType != GestureMilestoneType.UpHeadPitchMilestone
//            && gestureMilestoneType != GestureMilestoneType.DownHeadPitchMilestone) {
//            Log.d(TAG, "Head angle milestone type required but not provided!")
//        }
//    }
//
//    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
//
//        return when(gestureMilestoneType) {
//            GestureMilestoneType.DownHeadPitchMilestone ->
//                pitchAngle < PITCH_DOWN_PASS_ANGLE
//            GestureMilestoneType.UpHeadPitchMilestone ->
//                pitchAngle > PITCH_UP_PASS_ANGLE
//            else -> false
//        }
//    }
//}
//
//class HeadYawGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
//    : GestureMilestone(gestureMilestoneType) {
//
//    init {
//        if (gestureMilestoneType != GestureMilestoneType.OuterLeftHeadYawMilestone
//            && gestureMilestoneType != GestureMilestoneType.OuterRightHeadYawMilestone) {
//            Log.d(TAG, "Head angle milestone type required but not provided!")
//        }
//    }
//
//    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
//        return when(gestureMilestoneType) {
//            GestureMilestoneType.OuterLeftHeadYawMilestone ->
//                yawAngle < LEFT_YAW_PASS_ANGLE && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
//            GestureMilestoneType.OuterRightHeadYawMilestone ->
//                yawAngle > RIGHT_YAW_PASS_ANGLE && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
//            else -> false
//        }
//    }
//}
//
//class MouthGestureMilestone(val gestureMilestoneType: GestureMilestoneType)
//    : GestureMilestone(gestureMilestoneType) {
//
//    init {
//        if (gestureMilestoneType != GestureMilestoneType.MouthClosedMilestone
//            && gestureMilestoneType != GestureMilestoneType.MouthOpenMilestone) {
//            Log.d(TAG, "Mouth milestone type required but not provided!")
//        }
//    }
//
//    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
//        return when (gestureMilestoneType) {
//            GestureMilestoneType.MouthOpenMilestone ->
//                mouthFactor >= MOUTH_OPEN_PASS_FACTOR && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
//            GestureMilestoneType.MouthClosedMilestone ->
//                mouthFactor < MOUTH_OPEN_PASS_FACTOR && abs(pitchAngle) < PITCH_STRAIGHT_CHECK_ANGLE_ABS
//            else -> false
//        }
//    }
//}
//
//class CheckOverallHeadPositionMilestone(val gestureMilestoneType: GestureMilestoneType)
//    : GestureMilestone(gestureMilestoneType) {
//
//    //TODO: may add additional logic in next iterations
//    private fun areMilestoneTypesMet(): Boolean {
//        return true
//    }
//
//    override fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double): Boolean {
//        return true
//    }
//}


//enum class ObstacleType {
//    PITCH_ANGLE,
//    MULTIPLE_FACES_DETECTED,
//    NO_OR_PARTIAL_FACE_DETECTED,
//}
//
//const val PITCH_STRAIGHT_CHECK_ANGLE_ABS = 20.0
//const val PITCH_UP_PASS_ANGLE = 20.0
//const val PITCH_DOWN_PASS_ANGLE = -20.0
//
//const val LEFT_YAW_PASS_ANGLE = -30.0
//const val RIGHT_YAW_PASS_ANGLE = 30.0
//const val MOUTH_OPEN_PASS_FACTOR = 0.35  //reduced from 0.55 !


//
//open class GestureMilestone(val milestoneType: GestureMilestoneType) {
//
//    companion object {
//        const val TAG = "MILESTONES"
//    }
//
//    //can also add end extend yaw/roll if needed in the future
//    open fun isMet(pitchAngle: Double, mouthFactor: Double, yawAngle: Double) : Boolean {
//       throw NotImplementedError()
//    }
//}
//