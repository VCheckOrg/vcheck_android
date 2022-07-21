package com.vcheck.demo.dev.presentation.country_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.presentation.adapters.CountryListAdapter
import com.vcheck.demo.dev.databinding.CountryListFragmentBinding
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.presentation.adapters.SearchCountryCallback
import com.vcheck.demo.dev.util.ThemeWrapperFragment
import java.text.Collator
import java.util.*

class CountryListFragment : ThemeWrapperFragment(),
    CountryListAdapter.OnCountryItemClick, SearchCountryCallback {

    private lateinit var countriesList: List<CountryTO>
    private lateinit var appContainer: AppContainer
    private lateinit var binding: CountryListFragmentBinding
    private val args: CountryListFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.backgroundCountryList.background = ColorDrawable(Color.parseColor(it))
            binding.searchCountry.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.countryListBackArrow.setColorFilter(Color.parseColor(it))
            binding.tvNoCountriesFoundPlaceholder.setTextColor(Color.parseColor(it))

            val searchText =
                binding.searchCountry.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
            searchText.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            binding.searchCountryBorder.setCardBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (activity?.application as VCheckSDKApp).appContainer
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.country_list_fragment, container, false)
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

        changeColorsToCustomIfPresent()

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
        appContainer.mainRepository.storeSelectedCountryCode(
            activity as VCheckMainActivity,
            country
        )
        findNavController().popBackStack()
    }

    override fun onEmptySearchResult() {
        binding.countriesList.isVisible = false
        binding.tvNoCountriesFoundPlaceholder.isVisible = true
    }
}