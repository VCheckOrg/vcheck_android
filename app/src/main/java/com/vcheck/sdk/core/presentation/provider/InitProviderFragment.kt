package com.vcheck.sdk.core.presentation.provider

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.FragmentInitProviderBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.*
import com.vcheck.sdk.core.util.checkStageErrorForResult
import com.vcheck.sdk.core.util.checkUserInteractionCompletedForResult

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
            // ignoring any custom codes, also >= 0
            _viewModel.getCurrentStage()
        }

        _viewModel.stageResponse.observe(viewLifecycleOwner) {
            processStageData(it)
        }

        _viewModel.stageSpecificError.observe(viewLifecycleOwner) {
            (requireActivity() as AppCompatActivity)
                .checkStageErrorForResult(it?.errorData?.errorCode)
        }

        _viewModel.clientError.observe(viewLifecycleOwner) {

            (requireActivity() as AppCompatActivity)
                .checkUserInteractionCompletedForResult(it?.errorData?.errorCode)

            if (it != null) {
                _binding!!.startCallChainLoadingIndicator.isVisible = false
                Toast.makeText(activity, it.errorText, Toast.LENGTH_LONG).show()
            }
        }

        _viewModel.initProvider(initProviderRequestBody)
    }

    private fun processStageData(response: Resource<StageResponse>) {
        if (response.data?.errorCode != null
            && response.data.errorCode == StageErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
            findNavController().navigate(R.id.action_providerChosenFragment_to_livenessInstructionsFragment)
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
}