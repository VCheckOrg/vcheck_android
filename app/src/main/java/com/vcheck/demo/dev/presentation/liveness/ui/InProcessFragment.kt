package com.vcheck.demo.dev.presentation.liveness.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.presentation.liveness.LivenessActivity

class InProcessFragment : Fragment(R.layout.in_process_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as LivenessActivity).finishLivenessSession()
    }
}