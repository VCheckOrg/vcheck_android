package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
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
import com.vcheck.demo.dev.domain.PreProcessedDocData
import com.vcheck.demo.dev.presentation.MainActivity
import com.vcheck.demo.dev.presentation.adapters.CheckDocInfoAdapter
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

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        val currentLocaleCode = viewModel.repository.getLocale(activity as MainActivity)

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
        val adapter = CheckDocInfoAdapter(ArrayList(), this@CheckDocInfoFragment,
            currentLocaleCode)
        binding.docInfoList.adapter = adapter

        viewModel.documentInfoResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                populateDocFields(it.data.data, currentLocaleCode)
            }
        }

        viewModel.confirmedDocResponse.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.livenessInstructionsFragment)
            }
        }

        viewModel.getDocumentInfo(viewModel.repository.getVerifToken(activity as MainActivity),
            args.checkDocInfoDataTO.docId)

        binding.checkInfoConfirmButton.setOnClickListener {
            if (checkIfAnyFieldEmpty()) {
                Toast.makeText((activity as MainActivity),
                    R.string.check_doc_fields_validation_error, Toast.LENGTH_LONG).show()
            } else {
                viewModel.updateAndConfirmDocument(viewModel.repository.getVerifToken(activity as MainActivity),
                    args.checkDocInfoDataTO.docId, composeConfirmedDocFieldsData())
            }
        }
    }

    private fun populateDocFields(preProcessedDocData: PreProcessedDocData, currentLocaleCode: String) {
        if (preProcessedDocData.type.fields.isNotEmpty()) {
            Log.d("DOC", "GOT AUTO-PARSED FIELDS: ${preProcessedDocData.type.fields}")
            dataList = preProcessedDocData.type.fields.map { docField ->
                convertDocFieldToOptParsedData(docField, preProcessedDocData.parsedData)
            } as ArrayList<DocFieldWitOptPreFilledData>
            val updatedAdapter = CheckDocInfoAdapter(ArrayList(dataList),
                this@CheckDocInfoFragment, currentLocaleCode)
            binding.docInfoList.adapter = updatedAdapter
        } else {
            Log.i("DOC", "__NO__ AVAILABLE AUTO-PARSED FIELDS!")
        }
    }

    private fun checkIfAnyFieldEmpty(): Boolean {
        var hasValidationErrors: Boolean = false
        dataList.forEach {
            Log.d("DOC", "FIELD : ${it.autoParsedValue} | ${it.regex} | ${it.name} | ${it.title}")
            if (it.autoParsedValue.length < 2) {
                hasValidationErrors = true
            }
//            if (it.regex != null && it.autoParsedValue.matches(Regex(it.regex))) {
//                hasValidationErrors = true
//            }
        }
        return hasValidationErrors
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

    private fun convertDocFieldToOptParsedData(docField: DocField, parsedDocFieldsData: ParsedDocFieldsData?) : DocFieldWitOptPreFilledData {
        var optParsedData = ""
        if (parsedDocFieldsData == null) {
           return DocFieldWitOptPreFilledData(
                docField.name, docField.title, docField.type, docField.regex, "")
        } else {
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
                docField.name, docField.title, docField.type, docField.regex, optParsedData
            )
        }
    }
}