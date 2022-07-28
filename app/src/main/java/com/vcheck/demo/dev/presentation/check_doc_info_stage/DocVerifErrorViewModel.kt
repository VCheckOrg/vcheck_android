package com.vcheck.demo.dev.presentation.check_doc_info_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.domain.DocUserDataRequestBody
import com.vcheck.demo.dev.domain.ParsedDocFieldsData

class DocVerifErrorViewModel(val repository: MainRepository) : ViewModel() {

    var primaryDocStatusResponse: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setDocumentAsPrimary(docId: Int,  isForced: Boolean) {
        repository.updateAndConfirmDocInfo(docId, DocUserDataRequestBody(ParsedDocFieldsData(), isForced))
            .observeForever {
                primaryDocStatusResponse.value = true
        }
    }
}