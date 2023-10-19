package com.vcheck.sdk.core.presentation.provider

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentChooseProviderBinding
import com.vcheck.sdk.core.domain.Provider
import com.vcheck.sdk.core.presentation.adapters.ProvidersListAdapter
import com.vcheck.sdk.core.domain.ProviderLogicCase

class ChooseProviderFragment : Fragment(), ProvidersListAdapter.OnProviderItemClick {

    private var _binding: FragmentChooseProviderBinding? = null

    private val args: ChooseProviderFragmentArgs by navArgs()

    fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            _binding!!.chooseProviderBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            _binding!!.holderCard.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            _binding!!.chooseMethodTitle.setTextColor(Color.parseColor(it))
            _binding!!.backArrow.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            _binding!!.chooseMethodDescription.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_provider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChooseProviderBinding.bind(view)

        changeColorsToCustomIfPresent()

        val providerListAdapter = ProvidersListAdapter(
            args.chooseProviderLogicTO.providers, this@ChooseProviderFragment)

        _binding!!.providersList.adapter = providerListAdapter

        if (VCheckSDK.getProviderLogicCase() == ProviderLogicCase.ONE_PROVIDER_MULTIPLE_COUNTRIES ||
            VCheckSDK.getProviderLogicCase() == ProviderLogicCase.MULTIPLE_PROVIDERS_PRESENT_COUNTRIES) {
            _binding!!.backArrow.setOnClickListener {
                findNavController().popBackStack()
            }
        } else {
            _binding!!.backArrow.isVisible = false
        }
    }

    override fun onClick(provider: Provider) {
        VCheckSDK.setSelectedProvider(provider)
        findNavController().navigate(R.id.action_chooseProviderFragment_to_providerChosenFragment)
    }
}