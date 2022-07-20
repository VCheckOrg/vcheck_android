package com.vcheck.demo.dev.presentation.liveness.flow_logic

import java.lang.IndexOutOfBoundsException


enum class GestureMilestoneType {

    StraightHeadCheckMilestone,

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
            gmFromServiceValue(it)
        }
        stagesList = gestures
    }

    fun getCurrentStage(): GestureMilestoneType? {
        return if (currentStageIdx > (stagesList.size - 1)) {
            null
        } else {
            stagesList[currentStageIdx]
        }
    }

    fun getFirstStage(): GestureMilestoneType {
        return stagesList.getOrElse(0) { GestureMilestoneType.StraightHeadCheckMilestone }
    }

    fun areAllStagesPassed(): Boolean {
        return currentStageIdx > (stagesList.size - 1)
    }

    fun incrementCurrentStage() {
        currentStageIdx += 1
    }

    private fun gmFromServiceValue(strValue: String): GestureMilestoneType {
        return when(strValue) {
            "left" -> GestureMilestoneType.OuterLeftHeadYawMilestone
            "right" -> GestureMilestoneType.OuterRightHeadYawMilestone
            "up" -> GestureMilestoneType.UpHeadPitchMilestone
            "down" -> GestureMilestoneType.DownHeadPitchMilestone
            "mouth" -> GestureMilestoneType.MouthOpenMilestone
            else -> GestureMilestoneType.StraightHeadCheckMilestone
        }
    }

    fun getGestureRequestFromCurrentStage(): String {
        return if (currentStageIdx > (stagesList.size - 1)) {
            "straight"
        } else when(stagesList[currentStageIdx]) {
                GestureMilestoneType.OuterLeftHeadYawMilestone -> "left"
                GestureMilestoneType.OuterRightHeadYawMilestone -> "right"
                GestureMilestoneType.UpHeadPitchMilestone -> "up"
                GestureMilestoneType.DownHeadPitchMilestone -> "down"
                GestureMilestoneType.MouthOpenMilestone-> "mouth"
                else -> "straight"
            }
    }
}