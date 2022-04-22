package com.vcheck.demo.dev.presentation.screens

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.LivenessInstructionsFragmentBinding
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity
import com.vcheck.demo.dev.util.setMargins

class LivenessInstructionsFragment : Fragment(R.layout.liveness_instructions_fragment) {

    companion object {
        private const val HALF_BALL_ANIM_TIME: Long = 1000
    }

    private var binding: LivenessInstructionsFragmentBinding? = null

    private var isLeftCycle: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = LivenessInstructionsFragmentBinding.bind(view)

        binding!!.livenessStartButton.setOnClickListener {
            startActivity(Intent(activity as MainActivity, LivenessActivity::class.java))
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