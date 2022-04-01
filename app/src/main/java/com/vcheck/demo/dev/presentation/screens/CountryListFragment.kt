package com.vcheck.demo.dev.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.presentation.adapters.CountryListAdapter
import com.vcheck.demo.dev.databinding.CountryListFragmentBinding
import com.vcheck.demo.dev.domain.Country


class CountryListFragment : Fragment() {

    private lateinit var binding: CountryListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.country_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = CountryListFragmentBinding.bind(view)

        val countryList = arrayListOf<Country>(
            Country("Украина", R.drawable.ic_flag_ukraine),
            Country("Великобритания", R.drawable.ic_flag_united_kingdom),
            Country("Монголия", R.drawable.ic_flag_mongolia),
            Country("Тайланд", R.drawable.ic_flag_thailand),
            Country("Мексика", R.drawable.ic_flag_mexico),
            Country("Бразилия", R.drawable.ic_flag_brazil),
            Country("Германия", R.drawable.ic_flag_germany),
            Country("Израиль", R.drawable.ic_flag_israel),
        )

        val countryListAdapter = CountryListAdapter(countryList)
        binding.countriesList.adapter = countryListAdapter

        val navController = Navigation.findNavController(view)

        val chooseCard = view.findViewById<View>(R.id.country_list_back_arrow)
        chooseCard.setOnClickListener {
            //navController.navigate(R.id.action_countryListFragment_to_chooseCountryFragment)
        }
    }
}