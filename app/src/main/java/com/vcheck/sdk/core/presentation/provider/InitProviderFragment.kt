package com.vcheck.sdk.core.presentation.provider

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.FragmentInitProviderBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.presentation.VCheckStartupActivity

class InitProviderFragment : Fragment() {

    private var _binding: FragmentInitProviderBinding? = null

    private lateinit var _viewModel: InitProviderViewModel

    fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.fragmentDemoBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.startCallChainLoadingIndicator.setIndicatorColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = InitProviderViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_init_provider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentInitProviderBinding.bind(view)

        changeColorsToCustomIfPresent()

        val initProviderRequestBody = InitProviderRequestBody(VCheckSDK.getSelectedProvider().id,
            VCheckSDK.getOptSelectedCountryCode()) // country is optional here, may be nullable

        _viewModel.initProviderResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                _viewModel.getCurrentStage()
            }
        }

        _viewModel.stageResponse.observe(viewLifecycleOwner) {
            processStageData(it)
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
        }

        _viewModel.initProvider(initProviderRequestBody)
    }

    private fun processStageData(response: Resource<StageResponse>) {
        if (response.data?.errorCode != null
            && response.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
            findNavController().navigate(R.id.action_providerChosenFragment_to_livenessInstructionsFragment)
        } else if (response.data?.errorCode != null
            && response.data.errorCode == StageObstacleErrorType.VERIFICATION_EXPIRED.toTypeIdx()) {
            Toast.makeText(requireContext(), R.string.verification_expired, Toast.LENGTH_LONG).show()
            closeSDKFlow(shouldExecuteEndCallback = false)
        } else {
            if (response.data?.data != null) {
                val stageData = response.data.data
                if (stageData.type != StageType.LIVENESS_CHALLENGE.toTypeIdx()) {
                    checkDocStageDataForNavigation(stageData)
                } else {
                    if (stageData.config != null) {
                        _viewModel.repository.setLivenessMilestonesList((stageData.config.gestures))
                    }
                    findNavController().navigate(R.id.action_providerChosenFragment_to_livenessInstructionsFragment)
                }
            }
        }
    }

    private fun checkDocStageDataForNavigation(stageData: StageResponseData) {
        val action = if (stageData.uploadedDocId != null) {
            InitProviderFragmentDirections.actionProviderChosenFragmentToCheckDocInfoFragment(
                null, stageData.uploadedDocId)
        } else if (stageData.primaryDocId != null) {
            InitProviderFragmentDirections.actionProviderChosenFragmentToCheckDocInfoFragment(
                null, stageData.primaryDocId)
        } else {
            InitProviderFragmentDirections.actionProviderChosenFragmentToChooseDocMethodScreen()
        }
        findNavController().navigate(action)
    }

    private fun closeSDKFlow(shouldExecuteEndCallback: Boolean) {
        (VCheckDIContainer).mainRepository.setFirePartnerCallback(shouldExecuteEndCallback)
        (VCheckDIContainer).mainRepository.setFinishStartupActivity(true)
        val intents = Intent(requireActivity(), VCheckStartupActivity::class.java)
        intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intents)
    }
}