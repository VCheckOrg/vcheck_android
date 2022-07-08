package com.vcheck.demo.dev.presentation.check_doc_info_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.domain.ParsedDocFieldsData

class DocVerifErrorViewModel(val repository: MainRepository) : ViewModel() {

    var primaryDocStatusResponse: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setDocumentAsPrimary(token: String, docId: Int) {
        repository.updateAndConfirmDocInfo(token, docId, ParsedDocFieldsData()).observeForever {
            primaryDocStatusResponse.value = true //TODO TEST WITH NEW ARCH! + refactor for handling errors
        }
    }
}