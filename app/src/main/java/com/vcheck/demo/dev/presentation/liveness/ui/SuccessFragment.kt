package com.vcheck.demo.dev.presentation.liveness.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.SuccessFragmentBinding
import com.vcheck.demo.dev.presentation.StartupActivity
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity


class SuccessFragment : Fragment(R.layout.success_fragment) {

    private var _binding: SuccessFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = SuccessFragmentBinding.bind(view)

        _binding!!.successButton.setOnClickListener {
            //FOR TEST
            resetApplication()
        }
    }

    private fun resetApplication() {
//        val resetApplicationIntent = requireActivity().applicationContext
//            .packageManager.getLaunchIntentForPackage(requireActivity().applicationContext.packageName)
//
//        if (resetApplicationIntent != null) {
//            resetApplicationIntent.flags =
//                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        (activity as LivenessActivity).startActivity(resetApplicationIntent)
//        (context as LivenessActivity).overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        val intent = Intent(context, StartupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireActivity().startActivity(intent)
        if (context is Activity) {
            (context as Activity).finish()
        }
        Runtime.getRuntime().exit(0)
    }
}