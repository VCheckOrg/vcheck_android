package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.CheckDocInfoFragmentBinding
import com.vcheck.demo.dev.domain.DocField
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.adapters.CheckInfoAdapter

class CheckDocInfoFragment : Fragment(R.layout.check_doc_info_fragment) {

    private lateinit var binding: CheckDocInfoFragmentBinding
    private lateinit var viewModel: CheckDocInfoViewModel
    private lateinit var dataList: ArrayList<DocField>

    private val args: CheckDocInfoFragmentArgs by navArgs()

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
        val adapter = CheckInfoAdapter(dataList)
        binding.docInfoList.adapter = adapter

        viewModel.documentInfoResponse.observe(viewLifecycleOwner) {
            if (it.data?.data?.fields != null && it.data.data.fields.isNotEmpty()) {
                Log.d("DOC", "GOT AUTO-PARSED FIELDS: ${it.data.data.fields}")
                dataList = it.data.data.fields as ArrayList<DocField>
            } else {
                dataList = ArrayList(viewModel.repository.getSelectedDocTypeWithData().fields)
            }
            adapter.notifyDataSetChanged() //!
        }

        viewModel.getDocumentInfo(viewModel.getVerifToken(activity as MainActivity), args.checkDocInfoDataTO.docId)
    }
}