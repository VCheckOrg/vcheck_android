package com.vcheck.demo.dev.presentation.liveness.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.SuccessFragmentBinding
import com.vcheck.demo.dev.presentation.StartupActivity
//
//class SuccessFragment : Fragment(R.layout.success_fragment) {
//
//    private var _binding: SuccessFragmentBinding? = null
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        _binding = SuccessFragmentBinding.bind(view)
//
//        _binding!!.successButton.setOnClickListener {
//            //FOR TEST
//            resetApplication()
//        }
//    }
//
//    private fun resetApplication() {
//
//        val intent = Intent(context, StartupActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        requireActivity().startActivity(intent)
//        if (context is Activity) {
//            (context as Activity).finish()
//        }
//        Runtime.getRuntime().exit(0)
//    }
//}