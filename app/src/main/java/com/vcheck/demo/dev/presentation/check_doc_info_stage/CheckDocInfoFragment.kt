package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.CheckDocInfoFragmentBinding
import com.vcheck.demo.dev.domain.CountryTO
import com.vcheck.demo.dev.domain.DocField
import com.vcheck.demo.dev.domain.DocTitle
import com.vcheck.demo.dev.domain.DocTypeData
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.adapters.CheckInfoAdapter
import com.vcheck.demo.dev.presentation.doc_type_stage.ChooseDocMethodViewModel

class CheckDocInfoFragment : Fragment(R.layout.check_doc_info_fragment) {

    private lateinit var binding: CheckDocInfoFragmentBinding
    private lateinit var viewModel: CheckDocInfoViewModel
    private lateinit var dataList: ArrayList<DocField>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        viewModel =
            CheckDocInfoViewModel(appContainer.mainRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = CheckDocInfoFragmentBinding.bind(view)

        dataList = ArrayList()

        viewModel.getDocumentInfo(viewModel.getVerifToken(activity as MainActivity), 1)

        viewModel.documentInfoResponse.observe(viewLifecycleOwner) {
            //dataList = it.data?.data!!.fields as ArrayList<DocField>
        }

        val adapter = CheckInfoAdapter(dataList)

        binding.docInfoList.adapter = adapter
    }
}