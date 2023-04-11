package com.vcheck.sdk.core.presentation.start

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.FragmentDemoStartBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import java.util.*

internal class VCheckStartFragment : Fragment() {

    private var _binding: FragmentDemoStartBinding? = null

    private lateinit var _viewModel: VCheckStartViewModel

    private var verificationInitialized: Boolean = false

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                performStartupLogic()
            } else {
                PermissionErrDialog.newInstance(getString(R.string.permissions_denied))
                    .show(childFragmentManager, "permission_err_dialog")
            }
        }

    fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            _binding!!.btnStartDemoFlow.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.fragmentDemoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.startCallChainLoadingIndicator.setIndicatorColor(Color.parseColor(it))
            _binding!!.btnStartDemoFlow.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = VCheckStartViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_demo_start, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDemoStartBinding.bind(view)

        changeColorsToCustomIfPresent()

        _binding!!.startCallChainLoadingIndicator.isVisible = true

        _binding!!.startCallChainLoadingIndicator.isVisible = false
        _binding!!.btnStartDemoFlow.isVisible = false

        requestPermissionsLauncher.launch(
            arrayOf(Manifest.permission.CAMERA))
    }

    private fun performStartupLogic() {
        setResponseListeners()
        _viewModel.serviceTimestampRequest()
    }

    private fun setResponseListeners() {

        _viewModel.timestampResponse.observe(viewLifecycleOwner) {
            if (it.data != null) {
                _viewModel.initVerification()
            }
        }

        _viewModel.initResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null && !verificationInitialized) {

                verificationInitialized = true

                if (it.data.data.status > VerificationStatuses.WAITING_USER_INTERACTION) {
                    findNavController().navigate(R.id.action_demoStartFragment_to_verifSentFragment)
                } else {
                    _viewModel.getProviders() //!
                }
            }
        }

        _viewModel.providersResponse.observe(viewLifecycleOwner) {
            processProvidersData(it)
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false
                _binding!!.btnStartDemoFlow.isVisible = true
                _binding!!.btnStartDemoFlow.setOnClickListener {
                    performStartupLogic()
                }
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun processProvidersData(response: Resource<ProvidersResponse>) {
        if (response.data?.data != null) {
            val providersList = response.data.data
            if (providersList.isNotEmpty() && providersList.size == 1) {
                if (providersList.first().countries.isEmpty()) {
                    VCheckSDK.setSelectedProvider(providersList.first())
                    //TODO continue initialization
                } else if (providersList.first().countries.size == 1) {
                    VCheckSDK.setSelectedProvider(providersList.first())
                    VCheckSDK.setOptSelectedCountryCode(providersList.first().countries.first().code)
                    //TODO continue initialization
                } else {
                    findNavController().navigate(R.id.action_demoStartFragment_to_chooseCountryFragment)
                    //TODO send provider
                    //выводится страница выбора страны, пользователь выбирает страну
                    //выбирается провайдер с указанием этой страны
                    //TODO then continue initialization
                }
            } else if (providersList.isNotEmpty()) {
                findNavController().navigate()
            } else {
                Toast.makeText(activity, "Could not one or more retrieve valid providers", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Shows an error message dialog.  */
    class PermissionErrDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity as VCheckMainActivity
            return AlertDialog.Builder(activity)
                .setMessage(arguments?.getString(ARG_MESSAGE))
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    //activity.finish() //!
                    dismiss()
                }
                .create()
        }
        companion object {
            private const val ARG_MESSAGE = "message"
            fun newInstance(message: String?): PermissionErrDialog {
                val dialog =
                    PermissionErrDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.arguments = args
                return dialog
            }
        }
    }
}

// TODO: move to countries list fragment (?)
//
//        _viewModel.countriesResponse.observe(viewLifecycleOwner) {
//            if (it.data?.data != null) {
//                _binding!!.startCallChainLoadingIndicator.isVisible = false
//
//                val countryList = it.data.data.map { country ->
//                    val locale = Locale("", country.code)
//                    val flag = locale.country.toFlagEmoji()
//                    CountryTO(
//                        locale.displayCountry,
//                        country.code,
//                        flag,
//                        country.isBlocked)
//                }.toList() as ArrayList<CountryTO>
//                val action =
//                    VCheckStartFragmentDirections.actionDemoStartFragmentToChooseCountryFragment(
//                        CountriesListTO(countryList))
//                findNavController().navigate(action)
//            }
//        }