package com.vcheck.demo.dev.presentation.country_stage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.presentation.adapters.CountryListAdapter
import com.vcheck.demo.dev.databinding.CountryListFragmentBinding
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.adapters.SearchCountryCallback
import java.text.Collator
import java.util.*

class CountryListFragment : Fragment(R.layout.country_list_fragment),
    CountryListAdapter.OnCountryItemClick, SearchCountryCallback {

    private lateinit var countriesList: List<CountryTO>
    private lateinit var appContainer: AppContainer
    private lateinit var binding: CountryListFragmentBinding
    private val args: CountryListFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (activity?.application as VcheckDemoApp).appContainer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countriesList = args.countriesListTO.countriesList.map {

            when (it.code) {
                "us" -> CountryTO(
                    getString(R.string.united_states_of_america),
                    it.code,
                    it.flag,
                    it.isBlocked
                )
                "bm" -> CountryTO(
                    getString(R.string.bermuda),
                    it.code,
                    it.flag,
                    it.isBlocked
                )
                "tl" -> CountryTO(
                    getString(R.string.east_timor),
                    it.code,
                    it.flag,
                    it.isBlocked
                )
                else -> CountryTO(
                    Locale("", it.code).displayCountry.replace("&", "and"),
                    it.code,
                    it.flag,
                    it.isBlocked
                )
            }

        }.sortedWith { s1, s2 ->
            Collator.getInstance(Locale("")).compare(s1.name, s2.name)
        }.toList()

        binding = CountryListFragmentBinding.bind(view)

        binding.tvNoCountriesFoundPlaceholder.isVisible = false

        val countryListAdapter = CountryListAdapter(
            countriesList,
            this@CountryListFragment, this@CountryListFragment
        )

        binding.countriesList.adapter = countryListAdapter

        binding.searchCountry.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.countriesList.isVisible = true
                binding.tvNoCountriesFoundPlaceholder.isVisible = false
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                binding.countriesList.isVisible = true
                binding.tvNoCountriesFoundPlaceholder.isVisible = false
                countryListAdapter.filter.filter(newText)
                return false
            }
        })

        binding.countryListBackArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onClick(country: String) {
        appContainer.mainRepository.storeSelectedCountryCode(activity as MainActivity, country)
        findNavController().popBackStack()
    }

    override fun onEmptySearchResult() {
        binding.countriesList.isVisible = false
        binding.tvNoCountriesFoundPlaceholder.isVisible = true
    }
}