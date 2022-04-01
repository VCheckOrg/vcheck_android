package com.vcheck.demo.dev.presentation.start

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentDemoStartBinding

class DemoStartFragment : Fragment() {

    private var _binding: FragmentDemoStartBinding? = null

    private lateinit var _viewModel: DemoStartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = DemoStartViewModel(appContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_demo_start, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDemoStartBinding.bind(view)

        _viewModel.verifResponse.observe(viewLifecycleOwner) {
            _binding!!.tvCreateVerificationResultInfo.text =
                "application_id : ${it.data!!.data.applicationId} |" +
                        "redirect_url : ${it.data.data.redirectUrl} | create_time: ${it.data.data.createTime}"
        }

        _binding!!.btnStartDemoFlow.setOnClickListener {
            _viewModel.createTestVerificationRequest()
        }

        _binding!!.btnLaunchMediaPipeDemo.setOnClickListener {
            findNavController().navigate(R.id.action_demoStartFragment_to_livenessFragment2)
        }

    }

}