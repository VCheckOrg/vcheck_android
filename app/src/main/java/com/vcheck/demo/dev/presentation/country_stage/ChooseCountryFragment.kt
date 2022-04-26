package com.vcheck.demo.dev.presentation.country_stage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.ChooseCountryFragmentBinding
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.transferrable_objects.CountriesListTO
import com.vcheck.demo.dev.util.toFlagEmoji
import java.util.*

class ChooseCountryFragment : Fragment(R.layout.choose_country_fragment) {

    private lateinit var binding: ChooseCountryFragmentBinding
    lateinit var country: String
    private lateinit var appContainer: AppContainer
    private val args: ChooseCountryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (activity?.application as VcheckDemoApp).appContainer

//        if (!appContainer.mainRepository.isLocaleAutoChanged(activity as MainActivity)) {
//            appContainer.mainRepository.setLocaleAutoChanged(activity as MainActivity, true)
//            (activity as MainActivity).recreate()
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countryList = args.countriesListTO.countriesList

        binding = ChooseCountryFragmentBinding.bind(view)

        binding.chooseCountryCard.setOnClickListener {
            val action =
                ChooseCountryFragmentDirections.actionChooseCountryFragmentToCountryListFragment(
                    CountriesListTO(countryList)
                )
            findNavController().navigate(action)
        }

        binding.chooseCountryContinueButton.setOnClickListener {
            findNavController().navigate(R.id.action_chooseCountryFragment_to_chooseDocMethodScreen)
        }
    }

    override fun onResume() {
        super.onResume()
        country = appContainer.mainRepository.getSelectedCountryCode(activity as MainActivity)

        val locale = Locale("", country)

        val flag = locale.country.toFlagEmoji()

        binding.countryTitle.text = locale.displayCountry
        binding.flagEmoji.text = flag
    }
}