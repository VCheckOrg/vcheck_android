package com.vcheck.demo.dev.presentation.country_stage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        val chooseCard = view.findViewById<View>(R.id.choose_country_card)
        chooseCard.setOnClickListener {
            findNavController().navigate(R.id.action_chooseCountryFragment_to_countryListFragment)
        }
    }
}