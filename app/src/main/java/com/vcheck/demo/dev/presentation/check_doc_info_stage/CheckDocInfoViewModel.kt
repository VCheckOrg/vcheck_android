package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.ParsedDocFieldsData
import com.vcheck.demo.dev.domain.PreProcessedDocumentResponse

class CheckDocInfoViewModel(val repository: MainRepository) : ViewModel() {

    var confirmedDocResponse: MutableLiveData<Boolean> = MutableLiveData(false)

    var documentInfoResponse: MutableLiveData<Resource<PreProcessedDocumentResponse>> =
        MutableLiveData()

    fun getDocumentInfo(token: String, docId: Int) {
        repository.getDocumentInfo(token, docId).observeForever {
            documentInfoResponse.value = it
        }
    }

    fun updateAndConfirmDocument(token: String, docId: Int,
                                 parsedDocFieldsData: ParsedDocFieldsData) {
        Log.i("DOCUMENT", "UPDATING/CONFIRMING DOC: $parsedDocFieldsData")
        repository.updateAndConfirmDocInfo(token, docId, parsedDocFieldsData).observeForever {
            confirmedDocResponse.value = true //TODO refactor for handling errors
        }
    }
}