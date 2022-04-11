package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.CheckDocInfoFragmentBinding
import com.vcheck.demo.dev.domain.DocField
import com.vcheck.demo.dev.domain.DocFieldWitOptPreFilledData
import com.vcheck.demo.dev.domain.ParsedDocFieldsData
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.adapters.CheckInfoAdapter
import com.vcheck.demo.dev.presentation.adapters.DocInfoEditCallback

class CheckDocInfoFragment : Fragment(R.layout.check_doc_info_fragment), DocInfoEditCallback {

    private lateinit var binding: CheckDocInfoFragmentBinding
    private lateinit var viewModel: CheckDocInfoViewModel
    private lateinit var dataList: MutableList<DocFieldWitOptPreFilledData>

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

        binding.apply {
            photoCard2.isVisible = false

            val docImage1File = BitmapFactory.decodeFile(args.checkDocInfoDataTO.photo1Path)
            passportImage1.setImageBitmap(docImage1File)

            if (args.checkDocInfoDataTO.photo2Path != null) {
                photoCard2.isVisible = true
                val docImage2File = BitmapFactory.decodeFile(args.checkDocInfoDataTO.photo2Path)
                passportImage2.setImageBitmap(docImage2File)
            } else {
                photoCard2.isVisible = false
            }
        }

        dataList = mutableListOf()
        val adapter = CheckInfoAdapter(ArrayList(), this@CheckDocInfoFragment)
        binding.docInfoList.adapter = adapter

        viewModel.documentInfoResponse.observe(viewLifecycleOwner) {
            if (it.data?.data?.type?.fields != null && it.data.data.type.fields.isNotEmpty()) {
                Log.d("DOC", "GOT AUTO-PARSED FIELDS: ${it.data.data.type.fields}")
                dataList = it.data.data.type.fields.map { docField ->
                    convertDocFieldToOptParsedData(docField, it.data.data.parsedData)
                } as ArrayList<DocFieldWitOptPreFilledData>
                //adapter.notifyDataSetChanged() //remove
                val updatedAdapter = CheckInfoAdapter(ArrayList(dataList),
                    this@CheckDocInfoFragment)
                binding.docInfoList.adapter = updatedAdapter
            }
        }

        viewModel.confirmedDocResponse.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.livenessInstructionsFragment)
            }
        }

        viewModel.getDocumentInfo(viewModel.getVerifToken(activity as MainActivity), args.checkDocInfoDataTO.docId)

        binding.checkInfoConfirmButton.setOnClickListener {
            viewModel.updateAndConfirmDocument(viewModel.getVerifToken(activity as MainActivity),
                args.checkDocInfoDataTO.docId, composeConfirmedDocFieldsData())
        }
    }

    override fun onFieldInfoEdited(fieldName: String, value: String) {
         dataList.find { it.name == fieldName }?.autoParsedValue = value
    }

    private fun composeConfirmedDocFieldsData() : ParsedDocFieldsData {
        val data = ParsedDocFieldsData()
        dataList.forEach { docField ->
            if (docField.name == "date_of_birth") {
                data.dateOfBirth = docField.autoParsedValue
            }
            if (docField.name == "date_of_expiry") {
                data.dateOfExpiry = docField.autoParsedValue
            }
            if (docField.name == "name") {
                data.name = docField.autoParsedValue
            }
            if (docField.name == "surname") {
                data.surname = docField.autoParsedValue
            }
            if (docField.name == "number") {
                data.number = docField.autoParsedValue
            }
            if (docField.name == "og_name") {
                data.ogName = docField.autoParsedValue
            }
            if (docField.name == "og_surname") {
                data.ogSurname = docField.autoParsedValue
            }
        }
        return data
    }

    private fun convertDocFieldToOptParsedData(docField: DocField, parsedDocFieldsData: ParsedDocFieldsData) : DocFieldWitOptPreFilledData {
        var optParsedData = ""
        if (docField.name == "date_of_birth" && parsedDocFieldsData.dateOfBirth != null) {
            optParsedData = parsedDocFieldsData.dateOfBirth!!
        }
        if (docField.name == "date_of_expiry" && parsedDocFieldsData.dateOfExpiry != null) {
            optParsedData = parsedDocFieldsData.dateOfExpiry!!
        }
        if (docField.name == "name" && parsedDocFieldsData.name != null) {
            optParsedData = parsedDocFieldsData.name!!
        }
        if (docField.name == "surname" && parsedDocFieldsData.surname != null) {
            optParsedData = parsedDocFieldsData.surname!!
        }
        if (docField.name == "number" && parsedDocFieldsData.number != null) {
            optParsedData = parsedDocFieldsData.number!!
        }
        if (docField.name == "og_name" && parsedDocFieldsData.ogName != null) {
            optParsedData = parsedDocFieldsData.ogName!!
        }
        if (docField.name == "og_surname" && parsedDocFieldsData.ogSurname != null) {
            optParsedData = parsedDocFieldsData.ogSurname!!
        }
        return DocFieldWitOptPreFilledData(
            docField.name, docField.title, docField.type, docField.regex, optParsedData)
    }
}