package com.vcheck.demo.dev.presentation.screens

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.vcheck.demo.dev.R

class ChooseCountryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choose_country_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        val chooseCard = view.findViewById<View>(R.id.choose_country_card)
        chooseCard.setOnClickListener {
            //navController.navigate(R.id.action_chooseCountryFragment_to_countryListFragment)
        }
    }
}