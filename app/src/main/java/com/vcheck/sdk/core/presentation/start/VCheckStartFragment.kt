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
import com.vcheck.sdk.core.databinding.FragmentDemoStartBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckMainActivity
import com.vcheck.sdk.core.presentation.transferrable_objects.CountriesListTO
import com.vcheck.sdk.core.util.toFlagEmoji
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

        _binding!!.startCallChainLoadingIndicator.isVisible = true

        _binding!!.startCallChainLoadingIndicator.isVisible = false
        _binding!!.btnStartDemoFlow.isVisible = false

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
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

                _viewModel.getCurrentStage()
            }
        }

        _viewModel.stageResponse.observe(viewLifecycleOwner) {
            if (it.data?.errorCode != null
                        && it.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
                findNavController().navigate(R.id.action_demoStartFragment_to_livenessInstructionsFragment)
            } else {
                if (it.data?.data != null) {
                    if (it.data.data.uploadedDocId != null) {
                        val action = VCheckStartFragmentDirections.actionDemoStartFragmentToCheckDocInfoFragment(
                            null, it.data.data.uploadedDocId)
                        findNavController().navigate(action)
                    } else if (it.data.data.type == StageType.DOCUMENT_UPLOAD.toTypeIdx()) {
                        _viewModel.getCountriesList()
                    } else {
                        if (it.data.data.config != null) {
                            _viewModel.repository.setLivenessMilestonesList((it.data.data.config.gestures))
                        }
                        findNavController().navigate(R.id.action_demoStartFragment_to_livenessInstructionsFragment)
                    }
                }
            }
        }

        _viewModel.countriesResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false

                val countryList = it.data.data.map { country ->
                    val locale = Locale("", country.code)
                    val flag = locale.country.toFlagEmoji()
                    CountryTO(
                        locale.displayCountry,
                        country.code,
                        flag,
                        country.isBlocked)
                }.toList() as ArrayList<CountryTO>
                val action =
                    VCheckStartFragmentDirections.actionDemoStartFragmentToChooseCountryFragment(
                        CountriesListTO(countryList))
                findNavController().navigate(action)
            }
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