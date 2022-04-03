package com.vcheck.demo.dev.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.databinding.ChooseDocMethodFragmentBinding
import com.vcheck.demo.dev.domain.DocMethod
import com.vcheck.demo.dev.domain.DocMethodTO

class ChooseDocMethodFragment : Fragment() {

    private var _binding: ChooseDocMethodFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choose_doc_method_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ChooseDocMethodFragmentBinding.bind(view)

        _binding!!.docMethodInnerPassport.setOnClickListener {
            val action =
                ChooseDocMethodFragmentDirections.actionChooseDocMethodScreenToPhotoInstructionsFragment(
                    DocMethodTO(DocMethod.INNER_PASSPORT)
                )
            findNavController().navigate(action)
        }

        _binding!!.docMethodForeignPassport.setOnClickListener {
            val action =
                ChooseDocMethodFragmentDirections.actionChooseDocMethodScreenToPhotoInstructionsFragment(
                    DocMethodTO(DocMethod.FOREIGN_PASSPORT)
                )
            findNavController().navigate(action)
        }

        _binding!!.docMethodIdCard.setOnClickListener {
            val action =
                ChooseDocMethodFragmentDirections.actionChooseDocMethodScreenToPhotoInstructionsFragment(
                    DocMethodTO(DocMethod.ID_CARD)
                )
            findNavController().navigate(action)
        }
        //TODO: Pasha - add another 2 doc methods choosing logic + nav
    }
}