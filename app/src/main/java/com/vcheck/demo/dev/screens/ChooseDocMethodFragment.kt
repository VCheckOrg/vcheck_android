package com.vcheck.demo.dev.screens

import android.os.Bundle
import com.vcheck.demo.dev.screens.ChooseDocMethodFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R

class ChooseDocMethodFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choose_doc_method_fragment, container, false)
    }
}