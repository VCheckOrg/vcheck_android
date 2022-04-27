package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.ParsedDocFieldsData
import com.vcheck.demo.dev.domain.PreProcessedDocumentResponse
import retrofit2.Response

class CheckDocInfoViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var confirmedDocResponse: MutableLiveData<Resource<Response<Void>>?> = MutableLiveData(null)

    var documentInfoResponse: MutableLiveData<Resource<PreProcessedDocumentResponse>> = MutableLiveData()

    fun getDocumentInfo(token: String, docId: Int) {
        repository.getDocumentInfo(token, docId).observeForever {
            documentInfoResponse.value = it
        }
    }

    fun updateAndConfirmDocument(token: String, docId: Int,
                                 parsedDocFieldsData: ParsedDocFieldsData) {
        Log.i("DOCUMENT", "UPDATING/CONFIRMING DOC: $parsedDocFieldsData")
        repository.updateAndConfirmDocInfo(token, docId, parsedDocFieldsData)
            .observeForever {
                processConfirmResponse(it)
            }
    }

    private fun processConfirmResponse(response: Resource<Response<Void>>) {
        when(response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                confirmedDocResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }
}