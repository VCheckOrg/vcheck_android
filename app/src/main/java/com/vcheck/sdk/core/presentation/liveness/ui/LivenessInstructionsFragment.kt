package com.vcheck.sdk.core.presentation.liveness.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.LivenessInstructionsFragmentBinding
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import com.vcheck.sdk.core.util.extensions.setMargins
import java.util.*

class LivenessInstructionsFragment : ThemeWrapperFragment() {

    companion object {
        private const val HALF_BALL_ANIM_TIME: Long = 1000
        private const val PHONE_TO_FACE_CYCLE_INTERVAL: Long = 2000
    }

    private var binding: LivenessInstructionsFragmentBinding? = null

    private var currentCycleIdx = 1

    private var isLeftTurnSubCycle: Boolean = true

    override fun changeColorsToCustomIfPresent() {
        val drawable = binding!!.cosmeticRoundedFrame.background as GradientDrawable
        VCheckSDK.designConfig!!.primary?.let {
            binding!!.livenessStartButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            binding!!.livenessIstructionsBackground.background = ColorDrawable(Color.parseColor(it))
            drawable.setColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
            drawable.setStroke(5, Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            binding!!.faceCheckTitle.setTextColor(Color.parseColor(it))
            binding!!.requestedMovementsText.setTextColor(Color.parseColor(it))
            binding!!.smoothMovementsText.setTextColor(Color.parseColor(it))
            binding!!.noInterferenceText.setTextColor(Color.parseColor(it))
            binding!!.goodLightText.setTextColor(Color.parseColor(it))
            binding!!.fixedCameraText.setTextColor(Color.parseColor(it))
            //binding!!.livenessStartButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            binding!!.faceCheckDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primary?.let {
            binding!!.requestedMovementsIcon.setColorFilter(Color.parseColor(it))
            binding!!.smoothMovementsIcon.setColorFilter(Color.parseColor(it))
            binding!!.noInterferenceIcon.setColorFilter(Color.parseColor(it))
            binding!!.goodLightIcon.setColorFilter(Color.parseColor(it))
            binding!!.fixedCameraIcon.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.liveness_instructions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = LivenessInstructionsFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        binding!!.livenessStartButton.setOnClickListener {
            startActivity(Intent(activity as VCheckMainActivity, VCheckLivenessActivity::class.java))
        }

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    when (currentCycleIdx) {
                        1 -> {
                            startPhoneAnimCycle()
                        }
                        2 -> {
                            isLeftTurnSubCycle = true
                            startFaceSidesAnimation()
                        }
                        else -> {
                            isLeftTurnSubCycle = false
                            startFaceSidesAnimation()
                        }
                    }
                }
            }
        }, 0, PHONE_TO_FACE_CYCLE_INTERVAL)
    }

    fun startPhoneAnimCycle() {
        binding!!.faceAnimationView.cancelAnimation()

        binding!!.staticFaceAnimationView.isVisible = false

        binding!!.rightAnimBall.isVisible = false
        binding!!.leftAnimBall.isVisible = false

        binding!!.arrowAnimationView.isVisible = false

        binding!!.faceAnimationView.setAnimation(R.raw.face_plus_phone)
        binding!!.faceAnimationView.repeatCount = 1

        binding!!.faceAnimationView.scaleX = 1F
        binding!!.faceAnimationView.scaleY = 1F

        binding!!.faceAnimationView.playAnimation()

        currentCycleIdx += 1
    }

    fun startFaceSidesAnimation() {
        binding!!.faceAnimationView.cancelAnimation()

        if (isLeftTurnSubCycle) {
            binding!!.staticFaceAnimationView.isVisible = true
            binding!!.faceAnimationView.setAnimation(R.raw.left)

            binding!!.faceAnimationView.repeatCount = 0
            binding!!.arrowAnimationView.rotation = 0F

            binding!!.faceAnimationView.scaleX = 2F
            binding!!.faceAnimationView.scaleY = 2F

            binding!!.arrowAnimationView.setMargins(-120, 60,
                null, null)
            binding!!.faceAnimationView.playAnimation()

            binding!!.rightAnimBall.isVisible = false
            binding!!.leftAnimBall.isVisible = true

            binding!!.leftAnimBall.animate().alpha(1F).setDuration(HALF_BALL_ANIM_TIME).setInterpolator(
                DecelerateInterpolator()
            ).withEndAction {
                binding!!.leftAnimBall.animate().alpha(0F).setDuration(HALF_BALL_ANIM_TIME)
                    .setInterpolator(AccelerateInterpolator()).start()
            }.start()

            Handler(Looper.getMainLooper()).postDelayed({
                binding!!.staticFaceAnimationView.isVisible = false
            }, 300)
        } else {
            binding!!.staticFaceAnimationView.isVisible = true
            binding!!.faceAnimationView.setAnimation(R.raw.right)

            binding!!.faceAnimationView.repeatCount = 0
            binding!!.arrowAnimationView.rotation = 180F

            binding!!.faceAnimationView.scaleX = 2F
            binding!!.faceAnimationView.scaleY = 2F

            binding!!.arrowAnimationView.setMargins(120, 100,
                null, null)
            binding!!.faceAnimationView.playAnimation()

            binding!!.rightAnimBall.isVisible = true
            binding!!.leftAnimBall.isVisible = false

            binding!!.rightAnimBall.animate().alpha(1F).setDuration(HALF_BALL_ANIM_TIME).setInterpolator(
                DecelerateInterpolator()
            ).withEndAction {
                binding!!.rightAnimBall.animate().alpha(0F).setDuration(HALF_BALL_ANIM_TIME)
                    .setInterpolator(AccelerateInterpolator()).start()
            }.start()

            Handler(Looper.getMainLooper()).postDelayed({
                binding!!.staticFaceAnimationView.isVisible = false
            }, 300)
        }
        if (currentCycleIdx >= 3) {
            currentCycleIdx = 1
        } else {
            currentCycleIdx += 1
        }
    }

}