package com.vcheck.demo.dev.presentation.start

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentDemoStartBinding
import com.vcheck.demo.dev.presentation.MainActivity

class DemoStartFragment : Fragment() {

    private var _binding: FragmentDemoStartBinding? = null

    private lateinit var _viewModel: DemoStartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = DemoStartViewModel(appContainer.mainRepository, appContainer.localDatasource)
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
            if (it.data?.data != null) {
                _binding!!.tvCreateVerificationResultInfo.text =
                    "CREATED VERIFICATION REQUEST: application_id : ${it.data.data.applicationId} |" +
                            "redirect_url : ${it.data.data.redirectUrl}" //+create_time

                _viewModel.localDatasource.storeVerifToken((activity as MainActivity),
                    it.data.data.redirectUrl.substringAfterLast("/", "").substringBefore("?id"))
                _viewModel.setVerifToken(_viewModel.localDatasource.getVerifToken((activity as MainActivity)))
                _viewModel.initVerification()
            }
        }

        _viewModel.initResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.tvInitVerificationResultInfo.text =
                    "INITIALIZED VERIFICATION: document : ${it.data.data.document} |" +
                            "return_url : ${it.data.data.returnUrl} | stage: ${it.data.data.stage}" //+locale
                _viewModel.getCountriesList()
            }
        }

        _viewModel.countriesResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                Log.d("COUNTRIES", "GOT COUNTRIES: ${it.data.data.map { country -> country.code }.toList()}")
            }
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }

        _binding!!.btnStartDemoFlow.setOnClickListener {
            _viewModel.createTestVerificationRequest()
        }

        _binding!!.btnLaunchMediaPipeDemo.setOnClickListener {
            findNavController().navigate(R.id.action_demoStartFragment_to_livenessFragment2)
        }

    }

}