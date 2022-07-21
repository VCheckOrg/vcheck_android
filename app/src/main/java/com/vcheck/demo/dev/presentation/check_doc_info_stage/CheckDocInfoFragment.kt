package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.VCheckSDKApp
import com.vcheck.demo.dev.databinding.CheckDocInfoFragmentBinding
import com.vcheck.demo.dev.domain.*
import com.vcheck.demo.dev.presentation.VCheckMainActivity
import com.vcheck.demo.dev.presentation.adapters.CheckDocInfoAdapter
import com.vcheck.demo.dev.presentation.adapters.DocInfoEditCallback
import com.vcheck.demo.dev.presentation.liveness.VCheckLivenessActivity
import com.vcheck.demo.dev.util.ContextUtils
import com.vcheck.demo.dev.util.ThemeWrapperFragment
import java.io.File

class CheckDocInfoFragment : ThemeWrapperFragment(), DocInfoEditCallback {

    private lateinit var binding: CheckDocInfoFragmentBinding
    private lateinit var viewModel: CheckDocInfoViewModel
    private lateinit var dataList: MutableList<DocFieldWitOptPreFilledData>

    private val args: CheckDocInfoFragmentArgs by navArgs()

    private var uploadedDocID: Int? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.buttonsColorHex?.let {
            binding.checkInfoConfirmButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundPrimaryColorHex?.let {
            binding.checkDocInfoBackground.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundSecondaryColorHex?.let {
            binding.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.vcheckBackgroundTertiaryColorHex?.let {
            binding.photoCard1Background.setCardBackgroundColor(Color.parseColor(it))
            binding.photoCard2Background.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.textColorHex?.let {
            binding.checkFilledDataTitle.setTextColor(Color.parseColor(it))
            binding.checkInfoConfirmButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.descriptionTextColorHex?.let {
            binding.checkFilledDataDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            binding.photoCard1.setCardBackgroundColor(Color.parseColor(it))
            binding.photoCard2.setCardBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (activity?.application as VCheckSDKApp).appContainer
        viewModel =
            CheckDocInfoViewModel(appContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.check_doc_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = CheckDocInfoFragmentBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        val currentLocaleCode = ContextUtils.getSavedLanguage(activity as VCheckMainActivity)

        binding.apply {
            photoCard2.isVisible = false

            if (args.checkDocInfoDataTO != null) {
                Log.d("DOCUMENT", "===== ARGS TO : ${args.checkDocInfoDataTO}")
                uploadedDocID = args.checkDocInfoDataTO!!.docId

                val docPhoto1File = File(args.checkDocInfoDataTO!!.photo1Path)
                Picasso.get().load(docPhoto1File).fit().centerInside().into(passportImage1)

                if (args.checkDocInfoDataTO!!.photo2Path != null) {
                    photoCard2.isVisible = true
                    val docPhoto2File = File(args.checkDocInfoDataTO!!.photo2Path!!)
                    Picasso.get().load(docPhoto2File).fit().centerInside().into(passportImage2)
                } else {
                    photoCard2.isVisible = false
                }
            } else {
                Log.d("DOCUMENT", "===== ARGS TO IS MISSING! USING args.uplaodedDocId")
                uploadedDocID = args.uplaodedDocId
            }
        }

        dataList = mutableListOf()
        val adapter = CheckDocInfoAdapter(ArrayList(),
            this@CheckDocInfoFragment, currentLocaleCode)
        binding.docInfoList.adapter = adapter

        viewModel.documentInfoResponse.observe(viewLifecycleOwner) {
            if (it.data?.data != null) {
                populateDocFields(it.data.data, currentLocaleCode)
                //TODO!
                populatePrevUploadedDocPhotos()
            }
        }

        viewModel.confirmedDocResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.getCurrentStage(viewModel.repository.getVerifToken(activity as VCheckMainActivity))
            }
        }

        viewModel.stageResponse.observe(viewLifecycleOwner) {
            if (it.data?.errorCode == null || it.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
                if (it.data?.data?.config != null) {
                    viewModel.repository.setLivenessMilestonesList((it.data.data.config.gestures))
                    findNavController().navigate(R.id.action_checkDocInfoFragment_to_livenessInstructionsFragment)
                } else if (VCheckSDK.verificationClientCreationModel?.verificationType == VerificationSchemeType.DOCUMENT_UPLOAD_ONLY) {
                   finishSDKFlow()
                }
            } else {
                findNavController().navigate(R.id.action_global_demoStartFragment)
            }
        }

        viewModel.getDocumentInfo(viewModel.repository.getVerifToken(activity as VCheckMainActivity),
            uploadedDocID!!)


        binding.checkInfoConfirmButton.setOnClickListener {
            if (checkIfAnyFieldEmpty()) {
                Toast.makeText((activity as VCheckMainActivity),
                    R.string.check_doc_fields_validation_error, Toast.LENGTH_LONG).show()
            } else {
                viewModel.updateAndConfirmDocument(viewModel.repository.getVerifToken(activity as VCheckMainActivity),
                    uploadedDocID!!, composeConfirmedDocFieldsData())
            }
        }

        viewModel.clientError.observe(viewLifecycleOwner) {
            if (it != null) Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun populatePrevUploadedDocPhotos() {
        //TODO
    }

    private fun populateDocFields(preProcessedDocData: PreProcessedDocData, currentLocaleCode: String) {
        if (preProcessedDocData.type.docFields.isNotEmpty()) {
            Log.d("DOC", "GOT AUTO-PARSED FIELDS: ${preProcessedDocData.type.docFields}")
            dataList = preProcessedDocData.type.docFields.map { docField ->
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
        }
        return hasValidationErrors
    }

    override fun onFieldInfoEdited(fieldName: String, value: String) {
         dataList.find { it.name == fieldName }?.autoParsedValue = value
    }

    private fun composeConfirmedDocFieldsData() : DocUserDataRequestBody {
        val data = ParsedDocFieldsData()
        dataList.forEach { docField ->
            if (docField.name == "date_of_birth") {
                data.dateOfBirth = docField.autoParsedValue
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
        }
        return DocUserDataRequestBody(data)
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
            if (docField.name == "name" && parsedDocFieldsData.name != null) {
                optParsedData = parsedDocFieldsData.name!!
            }
            if (docField.name == "surname" && parsedDocFieldsData.surname != null) {
                optParsedData = parsedDocFieldsData.surname!!
            }
            if (docField.name == "number" && parsedDocFieldsData.number != null) {
                optParsedData = parsedDocFieldsData.number!!
            }
            return DocFieldWitOptPreFilledData(
                docField.name, docField.title, docField.type, docField.regex, optParsedData
            )
        }
    }

    private fun finishSDKFlow() {
        VCheckSDK.onFinish()
    }
}

// Deprecated fields / checks:

//            if (docField.name == "date_of_expiry" && parsedDocFieldsData.dateOfExpiry != null) {
//                optParsedData = parsedDocFieldsData.dateOfExpiry!!
//            }
//            if (docField.name == "og_name" && parsedDocFieldsData.ogName != null) {
//                optParsedData = parsedDocFieldsData.ogName!!
//            }
//            if (docField.name == "og_surname" && parsedDocFieldsData.ogSurname != null) {
//                optParsedData = parsedDocFieldsData.ogSurname!!
//            }