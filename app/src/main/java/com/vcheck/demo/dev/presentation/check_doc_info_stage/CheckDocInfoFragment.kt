package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
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
import com.vcheck.demo.dev.util.VCheckContextUtils
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
        VCheckSDK.backgroundPrimaryColorHex?.let {
            binding.checkDocInfoBackground.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundSecondaryColorHex?.let {
            binding.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.backgroundTertiaryColorHex?.let {
            binding.photoCard1Background.setCardBackgroundColor(Color.parseColor(it))
            binding.photoCard2Background.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.primaryTextColorHex?.let {
            binding.checkFilledDataTitle.setTextColor(Color.parseColor(it))
            binding.checkInfoConfirmButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.secondaryTextColorHex?.let {
            binding.checkFilledDataDescription.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.borderColorHex?.let {
            binding.photoCard1.setCardBackgroundColor(Color.parseColor(it))
            binding.photoCard2.setCardBackgroundColor(Color.parseColor(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = VCheckSDKApp.instance.appContainer
        viewModel = CheckDocInfoViewModel(appContainer.mainRepository)
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

        val currentLocaleCode = VCheckSDK.getSDKLangCode()

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
                Log.d("DOCUMENT", "===== ARGS TO IS MISSING! USING args.uploadedDocId")
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
                viewModel.getCurrentStage()
            }
        }

        viewModel.stageResponse.observe(viewLifecycleOwner) {
            if (it.data?.errorCode == null || it.data.errorCode == StageObstacleErrorType.USER_INTERACTED_COMPLETED.toTypeIdx()) {
                if (it.data?.data?.config != null) {
                    viewModel.repository.setLivenessMilestonesList((it.data.data.config.gestures))
                    findNavController().navigate(R.id.action_checkDocInfoFragment_to_livenessInstructionsFragment)
                } else if (VCheckSDK.verificationClientCreationModel?.verificationType == VerificationSchemeType.DOCUMENT_UPLOAD_ONLY) {
                   VCheckSDK.onApplicationFinish()
                }
            } else {
                findNavController().navigate(R.id.action_global_demoStartFragment)
            }
        }

        viewModel.getDocumentInfo(uploadedDocID!!)


        binding.checkInfoConfirmButton.setOnClickListener {
            if (checkIfAnyFieldEmpty()) {
                Toast.makeText((activity as VCheckMainActivity),
                    R.string.check_doc_fields_validation_error, Toast.LENGTH_LONG).show()
            } else {
                viewModel.updateAndConfirmDocument(uploadedDocID!!, composeConfirmedDocFieldsData())
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
        return DocUserDataRequestBody(data, args.checkDocInfoDataTO?.isForced ?: false)
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
}


//            } else if (_photo2Path != null && _photo1Path == null) {
////                _photo1Path = _photo2Path
////                _photo2Path = null
////                prepareForNavigation(true)