package com.vcheck.sdk.core.presentation.country_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ChooseCountryFragmentBinding
import com.vcheck.sdk.core.presentation.transferrable_objects.ChooseProviderLogicTO
import com.vcheck.sdk.core.presentation.transferrable_objects.CountriesListTO
import com.vcheck.sdk.core.presentation.transferrable_objects.ProviderLogicCase
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import com.vcheck.sdk.core.util.toFlagEmoji
import java.util.*

class ChooseCountryFragment : ThemeWrapperFragment() {

    lateinit var country: String
    private lateinit var binding: ChooseCountryFragmentBinding
    private val args: ChooseCountryFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            binding.chooseCountryContinueButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.chooseCountryBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            binding.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundTertiaryColorHex?.let {
            binding.chooseCountryCard.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.chooseCountryTitle.setTextColor(Color.parseColor(it))
            binding.chooseCountryCardTitle.setTextColor(Color.parseColor(it))
            binding.countryTitle.setTextColor(Color.parseColor(it))
            //binding.chooseCountryContinueButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            binding.chooseCountryDescription.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choose_country_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countryList = args.countriesListTO.countriesList

        binding = ChooseCountryFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        binding.chooseCountryCard.setOnClickListener {
            val action =
                ChooseCountryFragmentDirections.actionChooseCountryFragmentToCountryListFragment(
                    CountriesListTO(countryList))
            findNavController().navigate(action)
        }

        binding.chooseCountryContinueButton.setOnClickListener {
            chooseCountryOnProviderLogic()
        }
    }

    private fun chooseCountryOnProviderLogic() {

        when(VCheckSDK.getProviderLogicCase()) {
            ProviderLogicCase.ONE_PROVIDER_MULTIPLE_COUNTRIES -> {
                val action = ChooseCountryFragmentDirections
                    .actionChooseCountryFragmentToProviderChosenFragment()
                findNavController().navigate(action)
            }
            ProviderLogicCase.MULTIPLE_PROVIDERS_PRESENT_COUNTRIES -> {
                val countryCode = VCheckSDK.getOptSelectedCountryCode()
                //TODO optimize inner loops
                val distinctProvidersList = VCheckSDK.getAllAvailableProviders().filter {
                    it.countries!!.contains(countryCode) }
                val action = ChooseCountryFragmentDirections
                        .actionChooseCountryFragmentToChooseProviderFragment(
                            ChooseProviderLogicTO(distinctProvidersList))
                findNavController().navigate(action)
            }
            else -> {
                Toast.makeText(requireContext(), "Error: country options should not be available for that provider",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        country = VCheckSDK.getOptSelectedCountryCode()!!

        when (country) {
            "us" -> {
                val locale = Locale("", country)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = getString(R.string.united_states_of_america)
                binding.flagEmoji.text = flag
            }
            "bm" -> {
                val locale = Locale("", country)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = getString(R.string.bermuda)
                binding.flagEmoji.text = flag
            }
            "tl" -> {
                val locale = Locale("", country)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = getString(R.string.east_timor)
                binding.flagEmoji.text = flag
            }
            else -> {
                val locale = Locale("", country)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = locale.displayCountry.replace("&", "and")
                binding.flagEmoji.text = flag
            }
        }

    }
}