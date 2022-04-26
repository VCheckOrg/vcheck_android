package com.vcheck.demo.dev.presentation.start

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.FragmentDemoStartBinding
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.transferrable_objects.CountriesListTO
import com.vcheck.demo.dev.util.toFlagEmoji
import java.util.*

class DemoStartFragment : Fragment() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                //Stub; all is ok
            } else Toast.makeText(
                (activity as MainActivity),
                R.string.permissions_denied,
                Toast.LENGTH_LONG
            ).show()
        }

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

        _binding!!.startCallChainLoadingIndicator.isVisible = false

        _viewModel.verifResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.tvCreateVerificationResultInfo.text =
                    "CREATED VERIFICATION REQUEST: application_id : ${it.data.data.applicationId} |" +
                            "redirect_url : ${it.data.data.redirectUrl}" //+create_time

                if (it.data.data.redirectUrl != null) {
                    _viewModel.repository.storeVerifToken((activity as MainActivity),
                        it.data.data.redirectUrl!!.substringAfterLast("/", "")
                            .substringBefore("?id"))
                    _viewModel.setVerifToken(_viewModel.repository.getVerifToken((activity as MainActivity)))

                    _viewModel.initVerification()
                } else {
                    Toast.makeText(
                        (activity as MainActivity),
                        "Error: Cannot retrieve verification token",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        _viewModel.initResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.tvInitVerificationResultInfo.text =
                    "INITIALIZED VERIFICATION: document : ${it.data.data.document} |" +
                            "return_url : ${it.data.data.returnUrl} | stage: ${it.data.data.stage}" +
                            "| locale: ${it.data.data.locale}"

                _viewModel.getCountriesList()
            }
        }

        _viewModel.countriesResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false
                Log.d(
                    "COUNTRIES",
                    "GOT COUNTRIES: ${it.data.data.map { country -> country.code }.toList()}")

                val countryList = it.data.data.map { country ->
                    val locale = Locale("", country.code)
                    val flag = locale.country.toFlagEmoji()
                    CountryTO(locale.displayCountry, country.code, flag, country.isBlocked)
                }.toList() as ArrayList<CountryTO>

                val action =
                    DemoStartFragmentDirections.actionDemoStartFragmentToChooseCountryFragment(
                        CountriesListTO(countryList))
                findNavController().navigate(action)
            }
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }

        _binding!!.btnStartDemoFlow.setOnClickListener {
            _binding!!.startCallChainLoadingIndicator.isVisible = true
            _viewModel.createTestVerificationRequest(_viewModel.repository
                .getLocale(activity as MainActivity))
        }

        //! FOR TEST
        _binding!!.btnLaunchMediaPipeDemo.setOnClickListener {
            //TEMP nav action:
            findNavController().navigate(R.id.action_demoStartFragment_to_livenessInstructionsFragment)
            //startActivity(Intent(activity as MainActivity, LivenessActivity::class.java))
        }

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

}