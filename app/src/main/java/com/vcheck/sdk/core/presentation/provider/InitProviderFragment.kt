package com.vcheck.sdk.core.presentation.provider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.databinding.FragmentDemoStartBinding
import com.vcheck.sdk.core.domain.StageObstacleErrorType
import com.vcheck.sdk.core.domain.StageResponse
import com.vcheck.sdk.core.domain.StageType
import com.vcheck.sdk.core.domain.toTypeIdx
import com.vcheck.sdk.core.presentation.start.VCheckStartFragmentDirections

class InitProviderFragment : Fragment() {

    private var _binding: FragmentDemoStartBinding? = null

    private lateinit var _viewModel: InitProviderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_init_provider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        _viewModel.stageResponse.observe(viewLifecycleOwner) {
            processStageData(it)
        }

    }

    private fun processStageData(response: Resource<StageResponse>) {
        if (response.data?.errorCode != null
            && response.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
            findNavController().navigate(R.id.action_demoStartFragment_to_livenessInstructionsFragment)
        } else {
            if (response.data?.data != null) {
                val stageData = response.data.data
                if (stageData.uploadedDocId != null) {
                    val action = VCheckStartFragmentDirections.actionDemoStartFragmentToCheckDocInfoFragment(
                        null, stageData.uploadedDocId)
                    findNavController().navigate(action)
                } else if (stageData.primaryDocId != null) {
                    val action = VCheckStartFragmentDirections.actionDemoStartFragmentToCheckDocInfoFragment(
                        null, stageData.primaryDocId)
                    findNavController().navigate(action)
                }
                else if (stageData.type == StageType.DOCUMENT_UPLOAD.toTypeIdx()) {
                    //_viewModel.getCountriesList() //TODO check/remove countries logic from doc stage!
                } else {
                    if (stageData.config != null) {
                        _viewModel.repository.setLivenessMilestonesList((stageData.config.gestures))
                    }
                    findNavController().navigate(R.id.action_demoStartFragment_to_livenessInstructionsFragment)
                }
            }
        }
    }

}