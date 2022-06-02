package com.vcheck.demo.dev.presentation.start

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.FragmentDemoStartBinding
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.liveness.ui.CameraConnectionFragment
import com.vcheck.demo.dev.presentation.transferrable_objects.CountriesListTO
import com.vcheck.demo.dev.util.ContextUtils
import com.vcheck.demo.dev.util.toFlagEmoji
import java.util.*

class DemoStartFragment : Fragment() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                //Stub; all is ok
            } else {
                PermissionErrDialog.newInstance(getString(R.string.permissions_denied))
                    .show(childFragmentManager, "permission_err_dialog")
            }
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
                    _viewModel.repository.storeVerifToken(
                        (activity as MainActivity), it.data.data.token)

                    _viewModel.setVerifToken(_viewModel.repository.getVerifToken((activity as MainActivity)))

                    _viewModel.initVerification()
                } else {
                    Toast.makeText(
                        (activity as MainActivity),
                        "Error: Cannot retrieve verification token",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        _viewModel.initResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.tvInitVerificationResultInfo.text =
                    "INITIALIZED VERIFICATION: document : ${it.data.data.document} |" +
                            "return_url : ${it.data.data.returnUrl} | stage: ${it.data.data.stage}" +
                            "| locale: ${it.data.data.locale}"

                _viewModel.repository.storeMaxLivenessLocalAttempts(
                    (activity as MainActivity), it.data.data.livenessAttempts)
                _viewModel.getCountriesList()
            }
        }

        _viewModel.countriesResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false
                Log.d("COUNTRIES",
                    "GOT COUNTRIES: ${it.data.data.map { country -> country.code }.toList()}")

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
                    DemoStartFragmentDirections.actionDemoStartFragmentToChooseCountryFragment(
                        CountriesListTO(countryList)
                    )
                findNavController().navigate(action)
            }
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
        }

        _binding!!.btnStartDemoFlow.setOnClickListener {
            _binding!!.startCallChainLoadingIndicator.isVisible = true
            _viewModel.createTestVerificationRequest(
                ContextUtils.getSavedLanguage(activity as MainActivity)
            )
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
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )
    }

    /** Shows an error message dialog.  */
    class PermissionErrDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity as MainActivity
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