package com.vcheck.demo.dev.presentation.segmentation.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentSegmentationStartBinding

class SegmentationStartFragment : Fragment() {

    private var _binding: FragmentSegmentationStartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_segmentation_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSegmentationStartBinding.bind(view)
    }
}