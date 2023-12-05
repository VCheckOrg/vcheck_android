package com.vcheck.sdk.core.presentation.country_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentCountryListBinding
import com.vcheck.sdk.core.domain.CountryTO
import com.vcheck.sdk.core.presentation.adapters.CountryListAdapter
import com.vcheck.sdk.core.presentation.adapters.SearchCountryCallback
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment
import java.util.*


class CountryListFragment : ThemeWrapperFragment(),
    CountryListAdapter.OnCountryItemClick, SearchCountryCallback {

    private lateinit var countriesList: List<CountryTO>

    private var _binding: FragmentCountryListBinding? = null
    private val args: CountryListFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        val searchText = _binding!!.searchCountry
            .findViewById(androidx.appcompat.R.id.search_src_text) as TextView

        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.backgroundCountryList.background = ColorDrawable(Color.parseColor(it))
            _binding!!.searchCountry.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.tvNoCountriesFoundPlaceholder.setTextColor(Color.parseColor(it))
            searchText.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.secondaryTextColorHex?.let {
            searchText.setHintTextColor(Color.parseColor(it))
            val icon: ImageView = _binding!!.searchCountry
                .findViewById(androidx.appcompat.R.id.search_mag_icon)
            icon.setColorFilter(Color.parseColor(it))
            val clearBtn: ImageView = _binding!!.searchCountry
                .findViewById(androidx.appcompat.R.id.search_close_btn)
            clearBtn.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.sectionBorderColorHex?.let {
            _binding!!.searchCountryBorder.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.countryListBackArrowText.setTextColor(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_country_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCountryListBinding.bind(view)

        changeColorsToCustomIfPresent()

        setContent()
    }

    override fun onClick(country: String) {
        VCheckSDK.setOptSelectedCountryCode(country)
        findNavController().popBackStack()
    }

    override fun onEmptySearchResult() {
        _binding!!.countriesList.isVisible = false
        _binding!!.tvNoCountriesFoundPlaceholder.isVisible = true
    }

    private fun setContent() {

        setCountriesList(args.countriesListTO.countriesList)

        _binding!!.tvNoCountriesFoundPlaceholder.isVisible = false

        val countryListAdapter = CountryListAdapter(countriesList,
            this@CountryListFragment, this@CountryListFragment)

        _binding!!.countriesList.adapter = countryListAdapter

        _binding!!.searchCountry.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(p0: String?): Boolean {
                _binding!!.countriesList.isVisible = true
                _binding!!.tvNoCountriesFoundPlaceholder.isVisible = false
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                _binding!!.countriesList.isVisible = true
                _binding!!.tvNoCountriesFoundPlaceholder.isVisible = false
                countryListAdapter.filter.filter(newText)
                return false
            }
        })

        _binding!!.countryListBackArrowText.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setCountriesList(finalList: List<CountryTO>) {
        countriesList = finalList.map {

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

        }.toList()
    }
}