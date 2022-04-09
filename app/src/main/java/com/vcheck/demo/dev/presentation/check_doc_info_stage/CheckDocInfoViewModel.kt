package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.PreProcessedDocumentResponse

class CheckDocInfoViewModel(val repository: MainRepository) : ViewModel() {

    var documentInfoResponse: MutableLiveData<Resource<PreProcessedDocumentResponse>> =
        MutableLiveData()

    fun getVerifToken(ctx: Context): String {
        return repository.getVerifToken(ctx)
    }

    fun getDocumentInfo(token: String, docId: Int) {
        repository.getDocumentInfo(token, docId).observeForever {
            documentInfoResponse.value = it
        }
    }
}