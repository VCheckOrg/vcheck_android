package com.vcheck.sdk.core.presentation.provider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.databinding.FragmentChooseProviderBinding

class ChooseProviderFragment : Fragment() {

    private var _binding: FragmentChooseProviderBinding? = null

    private val args: ChooseProviderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_provider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChooseProviderBinding.bind(view)

        val protocolsList = args.chooseProviderLogicTO.providers.map { it.protocol }

        if (protocolsList.contains("vcheck")) {
            _binding!!.methodCardVcheck.isVisible = true
            _binding!!.methodCardVcheck.setOnClickListener {
                findNavController().navigate(R.id.action_chooseProviderFragment_to_providerChosenFragment)
            }
        }
    }
}