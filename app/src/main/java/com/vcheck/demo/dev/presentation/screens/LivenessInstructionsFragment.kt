package com.vcheck.demo.dev.presentation.screens

import android.animation.Animator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.databinding.LivenessInstructionsFragmentBinding
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.presentation.liveness.VCheckLivenessActivity
import com.vcheck.demo.dev.util.ThemeWrapperFragment
import com.vcheck.demo.dev.util.setMargins

class LivenessInstructionsFragment : ThemeWrapperFragment() {

    companion object {
        private const val HALF_BALL_ANIM_TIME: Long = 1000
    }

    private var binding: LivenessInstructionsFragmentBinding? = null

    private var isLeftCycle: Boolean = true

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            binding!!.livenessStartButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundPrimaryColorHex?.let {
            binding!!.livenessIstructionsBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundSecondaryColorHex?.let {
            binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.textColorHex?.let {
            binding!!.faceCheckTitle.setTextColor(Color.parseColor(it))
            binding!!.requestedMovementsText.setTextColor(Color.parseColor(it))
            binding!!.smoothMovementsText.setTextColor(Color.parseColor(it))
            binding!!.noInterferenceText.setTextColor(Color.parseColor(it))
            binding!!.goodLightText.setTextColor(Color.parseColor(it))
            binding!!.fixedCameraText.setTextColor(Color.parseColor(it))
            binding!!.requestedMovementsIcon.setColorFilter(Color.parseColor(it))
            binding!!.smoothMovementsIcon.setColorFilter(Color.parseColor(it))
            binding!!.noInterferenceIcon.setColorFilter(Color.parseColor(it))
            binding!!.goodLightIcon.setColorFilter(Color.parseColor(it))
            binding!!.fixedCameraIcon.setColorFilter(Color.parseColor(it))
            binding!!.livenessStartButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.descriptionTextColorHex?.let {
            binding!!.faceCheckDescription.setTextColor(Color.parseColor(it))
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

        binding!!.staticFaceAnimationView.repeatCount = 0
        binding!!.staticFaceAnimationView.pauseAnimation()
        binding!!.staticFaceAnimationView.isVisible = false

        binding!!.arrowAnimationView.rotation = 0F
        binding!!.arrowAnimationView.setMargins(-120, 60,
            null, null)

        binding!!.faceAnimationView.setAnimation(R.raw.left)
        binding!!.faceAnimationView.repeatCount = 0
        binding!!.faceAnimationView.playAnimation()

        binding!!.rightAnimBall.isVisible = false
        binding!!.leftAnimBall.isVisible = true

        binding!!.leftAnimBall.animate().alpha(1F).setDuration(HALF_BALL_ANIM_TIME).setInterpolator(
            DecelerateInterpolator()
        ).withEndAction {
            binding!!.leftAnimBall.animate().alpha(0F).setDuration(HALF_BALL_ANIM_TIME)
                .setInterpolator(AccelerateInterpolator()).start()
        }.start()

        binding!!.faceAnimationView.addAnimatorUpdateListener {
            if (it.currentPlayTime >= it.duration - 600) {
                binding!!.staticFaceAnimationView.isVisible = true
            }
        }

        binding!!.faceAnimationView.addAnimatorListener(object : Animator.AnimatorListener {

            override fun onAnimationEnd(animation: Animator) {
                isLeftCycle = !isLeftCycle
                makeAnotherAnimationCycle()
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

        })
    }

    fun makeAnotherAnimationCycle() {
        if (isLeftCycle) {
            binding!!.staticFaceAnimationView.isVisible = true
            binding!!.faceAnimationView.setAnimation(R.raw.left)
            binding!!.faceAnimationView.repeatCount = 0
            binding!!.arrowAnimationView.rotation = 0F
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
    }

    private fun getHalfDurationFaceAnim(): Long {
        return binding!!.faceAnimationView.duration / 2
    }
}