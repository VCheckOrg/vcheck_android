package com.vcheck.demo.dev.presentation.screens

import androidx.navigation.Navigation.findNavController
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import androidx.navigation.NavController

class LivenessInstructionsFragment : Fragment(R.layout.liveness_instructions_fragment)