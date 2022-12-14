package com.vcheck.sdk.core.presentation.country_stage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import com.vcheck.sdk.core.databinding.CountryListFragmentBinding
import com.vcheck.sdk.core.domain.CountryTO
import com.vcheck.sdk.core.presentation.adapters.CountryListAdapter
import com.vcheck.sdk.core.presentation.adapters.SearchCountryCallback
import com.vcheck.sdk.core.util.ThemeWrapperFragment
import java.text.Collator
import java.util.*


class CountryListFragment : ThemeWrapperFragment(),
    CountryListAdapter.OnCountryItemClick, SearchCountryCallback {

    private lateinit var countriesList: List<CountryTO>
    private lateinit var binding: CountryListFragmentBinding
    private val args: CountryListFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        val searchText = binding.searchCountry
            .findViewById(androidx.appcompat.R.id.search_src_text) as TextView
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.backgroundCountryList.background = ColorDrawable(Color.parseColor(it))
            binding.searchCountry.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.countryListBackArrow.setColorFilter(Color.parseColor(it))
            binding.tvNoCountriesFoundPlaceholder.setTextColor(Color.parseColor(it))
            searchText.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            searchText.setHintTextColor(Color.parseColor(it))
            //--
            val icon: ImageView = binding.searchCountry
                .findViewById(androidx.appcompat.R.id.search_mag_icon)
            icon.setColorFilter(Color.parseColor(it))
//            val whiteIcon: Drawable = icon.drawable
//            whiteIcon.setTint(Color.parseColor(it))
//            icon.setImageDrawable(whiteIcon)
            //--
            val clearBtn: ImageView = binding.searchCountry
                .findViewById(androidx.appcompat.R.id.search_close_btn)
            clearBtn.setColorFilter(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            binding.searchCountryBorder.setCardBackgroundColor(Color.parseColor(it))
        }
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
            SearchView.OnQueryTextListener {

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
        VCheckSDK.setSelectedCountryCode(country)
        findNavController().popBackStack()
    }

    override fun onEmptySearchResult() {
        binding.countriesList.isVisible = false
        binding.tvNoCountriesFoundPlaceholder.isVisible = true
    }
}