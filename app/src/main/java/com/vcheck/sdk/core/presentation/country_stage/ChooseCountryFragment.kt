package com.vcheck.sdk.core.presentation.country_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.ChooseCountryFragmentBinding
import com.vcheck.sdk.core.di.VCheckDIContainer
import com.vcheck.sdk.core.domain.CountryTO
import com.vcheck.sdk.core.presentation.transferrable_objects.ChooseProviderLogicTO
import com.vcheck.sdk.core.presentation.transferrable_objects.CountriesListTO
import com.vcheck.sdk.core.presentation.transferrable_objects.ProviderLogicCase
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import com.vcheck.sdk.core.util.checkUserInteractionCompletedForResult
import com.vcheck.sdk.core.util.toFlagEmoji
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList

class ChooseCountryFragment : ThemeWrapperFragment() {

    private lateinit var binding: ChooseCountryFragmentBinding
    private val args: ChooseCountryFragmentArgs by navArgs()

    private lateinit var _viewModel: ChooseCountryViewModel

    private var finalCountries: List<CountryTO> = emptyList()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel = ChooseCountryViewModel(VCheckDIContainer.mainRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = ChooseCountryFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _viewModel.priorityCountriesResponse.observe(viewLifecycleOwner) {

            (requireActivity() as AppCompatActivity)
                .checkUserInteractionCompletedForResult(it.data?.errorCode)

            if (it.data != null) {
                setContent(it.data.data ?: emptyList())
            }
        }

        _viewModel.getPriorityCountries()
    }

    private fun setContent(priorityCountries: List<String>) {

        val initialCountryList = args.countriesListTO.countriesList

        val allCountryItems = emptyList<CountryTO>().toMutableList()

        val bottomCountryItems = emptyList<CountryTO>().toMutableList()

        initialCountryList.forEach { countryTO ->
            if (!priorityCountries.contains(countryTO.code)) {
                bottomCountryItems += countryTO
            }
        }

        val bottomCountryItemsSorted = bottomCountryItems.sortedWith { s1, s2 ->
            Collator.getInstance(Locale("")).compare(s1.name, s2.name) }.toList()

        var topCountryItems: List<CountryTO> = emptyList()

        try {
            topCountryItems = priorityCountries.map { code ->
                initialCountryList.first { it.code == code }
            }
        } catch (e: NoSuchElementException) {
            Log.d(VCheckSDK.TAG, "No country was found while doing comparison!")
        }

        allCountryItems.addAll(topCountryItems)
        allCountryItems.addAll(bottomCountryItemsSorted)

        finalCountries = ArrayList(allCountryItems)

        reloadData()

        binding.chooseCountryCard.setOnClickListener {
            val action =
                ChooseCountryFragmentDirections.actionChooseCountryFragmentToCountryListFragment(
                    CountriesListTO(finalCountries as ArrayList<CountryTO>))
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
                val countryCode = VCheckSDK.getOptSelectedCountryCode()!!
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

    private fun reloadData() {

        val selectedCountryCode : String = if (VCheckSDK.getOptSelectedCountryCode() != null) {
            finalCountries.first { it.code == VCheckSDK.getOptSelectedCountryCode() }.code
        } else if (finalCountries.map { it.code }.contains("ua")) {
            "ua"
        } else {
            finalCountries.first().code
        }

        VCheckSDK.setOptSelectedCountryCode(selectedCountryCode)

        when (selectedCountryCode) {
            "us" -> {
                val locale = Locale("", selectedCountryCode)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = getString(R.string.united_states_of_america)
                binding.flagEmoji.text = flag
            }
            "bm" -> {
                val locale = Locale("", selectedCountryCode)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = getString(R.string.bermuda)
                binding.flagEmoji.text = flag
            }
            "tl" -> {
                val locale = Locale("", selectedCountryCode)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = getString(R.string.east_timor)
                binding.flagEmoji.text = flag
            }
            else -> {
                val locale = Locale("", selectedCountryCode)

                val flag = locale.country.toFlagEmoji()

                binding.countryTitle.text = locale.displayCountry.replace("&", "and")
                binding.flagEmoji.text = flag
            }
        }

    }
}