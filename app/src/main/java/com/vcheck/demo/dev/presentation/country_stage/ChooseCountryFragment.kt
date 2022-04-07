package com.vcheck.demo.dev.presentation.country_stage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.ChooseCountryFragmentBinding
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.domain.CountriesListTO
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.MainActivity
import java.util.*

class ChooseCountryFragment : Fragment(R.layout.choose_country_fragment) {

    private lateinit var binding: ChooseCountryFragmentBinding
    private lateinit var viewModel: ChooseCountryViewModel
    lateinit var country: String
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = (activity?.application as VcheckDemoApp).appContainer
        viewModel =
            ChooseCountryViewModel(appContainer.mainRepository)
        viewModel.setVerifToken(appContainer.localDatasource.getVerifToken(activity as MainActivity))
        viewModel.getCountriesList()

    }

    override fun onResume() {
        super.onResume()
        country = appContainer.localDatasource.getChosenCountry(activity as MainActivity)

        val locale = Locale("", country)
        val firstLetter: Int = Character.codePointAt(locale.country, 0) - 0x41 + 0x1F1E6
        val secondLetter: Int =
            Character.codePointAt(locale.country, 1) - 0x41 + 0x1F1E6
        val flag = String(Character.toChars(firstLetter)) + String(
            Character.toChars(secondLetter)
        )

        binding.countryTitle.text = locale.displayCountry
        binding.flagEmoji.text = flag
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var countryList = ArrayList<CountryTO>()

        viewModel.countriesResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                countryList = it.data.data.map { country ->
                    val locale = Locale("", country.code)
                    val firstLetter: Int = Character.codePointAt(locale.country, 0) - 0x41 + 0x1F1E6
                    val secondLetter: Int =
                        Character.codePointAt(locale.country, 1) - 0x41 + 0x1F1E6
                    val flag = String(Character.toChars(firstLetter)) + String(
                        Character.toChars(secondLetter)
                    )
                    CountryTO(locale.displayCountry, country.code, flag)
                }.toList() as ArrayList<CountryTO>
            }
        }

        viewModel.clientError.observe(viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }

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
}