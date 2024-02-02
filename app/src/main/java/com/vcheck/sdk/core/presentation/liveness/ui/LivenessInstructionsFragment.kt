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
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentLivenessInstructionsBinding
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import com.vcheck.sdk.core.util.extensions.setMargins
import java.util.*

class LivenessInstructionsFragment : ThemeWrapperFragment() {

    companion object {
        private const val HALF_BALL_ANIM_TIME: Long = 1000
        private const val PHONE_TO_FACE_CYCLE_INTERVAL: Long = 2000
        private const val FACE_FADE_DURATION: Long = 550
    }

    private var binding: FragmentLivenessInstructionsBinding? = null

    private var currentCycleIdx = 1

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
        return inflater.inflate(R.layout.fragment_liveness_instructions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLivenessInstructionsBinding.bind(view)

        changeColorsToCustomIfPresent()

        binding!!.livenessStartButton.setOnClickListener {
            startActivity(Intent(activity as VCheckMainActivity, VCheckLivenessActivity::class.java))
        }

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    when (currentCycleIdx) {
                        1 -> startPhoneAnimCycle()
                        2 -> startFaceSidesAnimationCycle()
                        3 -> startFaceSidesAnimationCycle()
                        4 -> startMouthOpeningCycle()
                    }
                }
            }
        }, 0, PHONE_TO_FACE_CYCLE_INTERVAL)
    }

    fun startPhoneAnimCycle() {

        binding!!.faceAnimHolder.isVisible = false

        binding!!.faceAnimationView.cancelAnimation()

        binding!!.rightAnimBall.isVisible = false
        binding!!.leftAnimBall.isVisible = false

        binding!!.arrowAnimationView.isVisible = false

        binding!!.faceAnimationView.setAnimation(R.raw.face_plus_phone)
        binding!!.faceAnimationView.repeatCount = 1

        binding!!.faceAnimationView.setMargins(null, 0,
            null, 0)

        binding!!.faceAnimationView.playAnimation()

        fadeFaceAnimInForTransition()
        binding!!.faceAnimHolder.isVisible = true

        currentCycleIdx += 1

        Handler(Looper.getMainLooper()).postDelayed({
            fadeFaceAnimOutForTransition()
        }, PHONE_TO_FACE_CYCLE_INTERVAL - FACE_FADE_DURATION)
    }

    fun startFaceSidesAnimationCycle() {
        binding!!.faceAnimationView.cancelAnimation()

        if (currentCycleIdx == 2) {

            binding!!.faceAnimationView.setAnimation(R.raw.left)
            binding!!.faceAnimationView.setMargins(null, 10,
                null, -10)

            binding!!.faceAnimationView.repeatCount = 0
            binding!!.arrowAnimationView.rotation = 0F

            binding!!.arrowAnimationView.setMargins(-120, 60,
                null, null)
            binding!!.faceAnimationView.playAnimation()

            binding!!.rightAnimBall.isVisible = false
            binding!!.leftAnimBall.isVisible = true

            binding!!.leftAnimBall.animate().alpha(1F)
                .setDuration(HALF_BALL_ANIM_TIME)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    binding!!.leftAnimBall.animate().alpha(0F)
                        .setDuration(HALF_BALL_ANIM_TIME)
                        .setInterpolator(AccelerateInterpolator()).start()
                }.start()

            fadeFaceAnimInForTransition()

            Handler(Looper.getMainLooper()).postDelayed({
                fadeFaceAnimOutForTransition()
            }, PHONE_TO_FACE_CYCLE_INTERVAL - FACE_FADE_DURATION)

            currentCycleIdx += 1

        } else {
            binding!!.faceAnimationView.setAnimation(R.raw.right)
            binding!!.faceAnimationView.setMargins(null, 10,
                null, -10)

            binding!!.faceAnimationView.repeatCount = 0
            binding!!.arrowAnimationView.rotation = 180F

            binding!!.arrowAnimationView.setMargins(120, 100,
                null, null)
            binding!!.faceAnimationView.playAnimation()

            binding!!.rightAnimBall.isVisible = true
            binding!!.leftAnimBall.isVisible = false

            binding!!.rightAnimBall.animate().alpha(1F)
                .setDuration(HALF_BALL_ANIM_TIME)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    binding!!.rightAnimBall.animate().alpha(0F)
                        .setDuration(HALF_BALL_ANIM_TIME)
                        .setInterpolator(AccelerateInterpolator()).start()
                }.start()

            fadeFaceAnimInForTransition()

            Handler(Looper.getMainLooper()).postDelayed({
                fadeFaceAnimOutForTransition()
            }, PHONE_TO_FACE_CYCLE_INTERVAL - FACE_FADE_DURATION)

            currentCycleIdx += 1
        }
    }

    fun startMouthOpeningCycle() {
        binding!!.faceAnimationView.cancelAnimation()

        binding!!.rightAnimBall.isVisible = false
        binding!!.leftAnimBall.isVisible = false

        binding!!.arrowAnimationView.isVisible = false

        binding!!.faceAnimationView.setAnimation(R.raw.mouth)
        binding!!.faceAnimationView.repeatCount = 1

        binding!!.faceAnimationView.setMargins(null, 0,
            null, 0)

        binding!!.faceAnimationView.playAnimation()

        fadeFaceAnimInForTransition()

        Handler(Looper.getMainLooper()).postDelayed({
            fadeFaceAnimOutForTransition()
        }, PHONE_TO_FACE_CYCLE_INTERVAL - FACE_FADE_DURATION)

        currentCycleIdx = 1
    }

    private fun fadeFaceAnimInForTransition() {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = FACE_FADE_DURATION

        val animation = AnimationSet(false) //change to false
        animation.addAnimation(fadeIn)
        binding!!.faceAnimHolder.animation = animation
    }

    private fun fadeFaceAnimOutForTransition() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator() //and this
        fadeOut.duration = FACE_FADE_DURATION

        val animation = AnimationSet(false) //change to false
        animation.addAnimation(fadeOut)
        binding!!.faceAnimHolder.animation = animation
    }
}