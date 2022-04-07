package com.vcheck.demo.dev.presentation.country_stage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.presentation.adapters.CountryListAdapter
import com.vcheck.demo.dev.databinding.CountryListFragmentBinding
import com.vcheck.demo.dev.di.AppContainer
import com.vcheck.demo.dev.domain.Country
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.photo_upload_stage.PhotoInstructionsFragmentArgs


class CountryListFragment : Fragment(R.layout.country_list_fragment),
    CountryListAdapter.OnCountryItemClick {

    private lateinit var countriesList: ArrayList<CountryTO>
    private lateinit var appContainer: AppContainer
    private lateinit var binding: CountryListFragmentBinding
    private val args: CountryListFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (activity?.application as VcheckDemoApp).appContainer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countriesList = args.countriesListTO.countriesList

        binding = CountryListFragmentBinding.bind(view)

        val countryListAdapter = CountryListAdapter(countriesList, this)
        binding.countriesList.adapter = countryListAdapter

        binding.searchCountry.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                countryListAdapter.filter.filter(newText)
                return false
            }
        })

        binding.countryListBackArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onClick(position: Int) {
        val country = countriesList[position].code
        appContainer.localDatasource.storeChosenCountry(activity as MainActivity, country)

        findNavController().popBackStack()
    }
}