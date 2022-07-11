package com.vcheck.demo.dev.presentation.start

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.databinding.FragmentDemoStartBinding
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.presentation.liveness.VCheckLivenessActivity
import com.vcheck.demo.dev.presentation.transferrable_objects.CountriesListTO
import com.vcheck.demo.dev.util.ContextUtils
import com.vcheck.demo.dev.util.ThemeWrapperFragment
import com.vcheck.demo.dev.util.toFlagEmoji
import java.util.*

internal class VCheckStartFragment : ThemeWrapperFragment() {

    private lateinit var appContainer: AppContainer

    private var _binding: FragmentDemoStartBinding? = null

    private lateinit var _viewModel: VCheckStartViewModel

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                performStartupLogic()
            } else {
                PermissionErrDialog.newInstance(getString(R.string.permissions_denied))
                    .show(childFragmentManager, "permission_err_dialog")
            }
        }

    //TODO test!
    override fun changeColorsToCustomIfPresent() {
        if (VCheckSDK.buttonsColorHex != null) {
            _binding!!.btnStartDemoFlow.background = ColorDrawable(Color.parseColor(VCheckSDK.buttonsColorHex))
        }
        if (VCheckSDK.backgroundColorHex != null) {
            _binding!!.background.background = ColorDrawable(Color.parseColor(VCheckSDK.backgroundColorHex))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = (activity?.application as VCheckSDKApp).appContainer
        _viewModel = VCheckStartViewModel(appContainer.mainRepository)
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

        _binding!!.startCallChainLoadingIndicator.isVisible = false
        _binding!!.btnStartDemoFlow.isVisible = false

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun performStartupLogic() {
        //Launching main flow if all permissions are set
        _binding!!.startCallChainLoadingIndicator.isVisible = true
        if (VCheckSDK.verificationClientCreationModel == null) {
            Toast.makeText(activity, "Client error: Verification was not created properly", Toast.LENGTH_LONG).show()
        } else {
            setResponseListeners()
            _viewModel.serviceTimestampRequest()
        }
    }

    private fun setResponseListeners() {

        _viewModel.timestampResponse.observe(viewLifecycleOwner) {
            if (it.data != null) {
                val requestModel = _viewModel.repository.prepareVerificationRequest(
                    it.data.toLong(), ContextUtils.getSavedLanguage(activity as VCheckMainActivity),
                    VCheckSDK.verificationClientCreationModel!!)
                Log.d("REQUEST MODEL"," : $requestModel")
                _viewModel.createVerificationRequest(requestModel)
            }
        }

        _viewModel.createResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _viewModel.repository.storeVerifToken(
                    (activity as VCheckMainActivity), it.data.data.token)

                _viewModel.setVerifToken(_viewModel.repository.getVerifToken((activity as VCheckMainActivity)))

                _viewModel.initVerification()
            }
        }

        _viewModel.initResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _viewModel.getCurrentStage()
            }
        }

        _viewModel.stageResponse.observe(viewLifecycleOwner) {
            if (it.data?.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
                startActivity(Intent(activity as VCheckMainActivity, VCheckLivenessActivity::class.java))
            } else {
                if (it.data?.data != null) {
                    Log.d("STAGING", "----- CURRENT STAGE TYPE: ${it.data.data.type}")
                    if (it.data.data.uploadedDocId != null) {
                        val action = VCheckStartFragmentDirections.actionDemoStartFragmentToCheckDocInfoFragment(
                            null, it.data.data.uploadedDocId)
                        findNavController().navigate(action)
                    } else if (it.data.data.type == StageType.DOCUMENT_UPLOAD.toTypeIdx()) {
                        _viewModel.getCountriesList()
                    } else {
                        startActivity(Intent(activity as VCheckMainActivity, VCheckLivenessActivity::class.java))
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


//if (_viewModel.repository.checkIfApiConfigShouldBeChanged(VCheckSDK.verificationClientCreationModel!!)) {
//    appContainer.updateVerificationApiConfigs(
//        VCheckSDK.verificationClientCreationModel!!.customVerificationServiceURL!!,
//        VCheckSDK.verificationClientCreationModel!!.customPartnerServiceURL!!)
//    _viewModel = DemoStartViewModel(appContainer.mainRepository)
//}

//        _binding!!.btnStartDemoFlow.setOnClickListener {
//            _binding!!.startCallChainLoadingIndicator.isVisible = true
//            _viewModel.createTestVerificationRequest(
//                ContextUtils.getSavedLanguage(activity as VCheckMainActivity))
//        }

//! FOR TEST
//        _binding!!.btnLaunchMediaPipeDemo.setOnClickListener {
//            //TEMP nav action:
//            findNavController().navigate(R.id.action_demoStartFragment_to_livenessInstructionsFragment)
//            //startActivity(Intent(activity as MainActivity, LivenessActivity::class.java))
//        }

//Obsolete binding text indications:

//                _binding!!.tvCreateVerificationResultInfo.text =
//                    "CREATED VERIFICATION REQUEST: application_id : ${it.data.data.applicationId} |" +
//                            "redirect_url : ${it.data.data.redirectUrl}" //+create_time

//                _binding!!.tvInitVerificationResultInfo.text =
//                    "INITIALIZED VERIFICATION: document : ${it.data.data.document} |" +
//                            "return_url : ${it.data.data.returnUrl} | stage: ${it.data.data.stage}" +
//                            "| locale: ${it.data.data.locale}"

//                Log.d("COUNTRIES",
//                    "GOT COUNTRIES: ${it.data.data.map { country -> country.code }.toList()}")