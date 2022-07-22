package com.vcheck.demo.dev.presentation.check_doc_info_stage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.DocUserDataRequestBody
import com.vcheck.demo.dev.domain.PreProcessedDocumentResponse
import com.vcheck.demo.dev.domain.StageResponse
import retrofit2.Response

class CheckDocInfoViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var confirmedDocResponse: MutableLiveData<Resource<Response<Void>>?> = MutableLiveData(null)

    var documentInfoResponse: MutableLiveData<Resource<PreProcessedDocumentResponse>> = MutableLiveData()

    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    fun getDocumentInfo(docId: Int) {
        repository.getDocumentInfo(docId).observeForever {
            documentInfoResponse.value = it
        }
    }

    fun updateAndConfirmDocument(docId: Int,
                                 userData: DocUserDataRequestBody) {
        Log.i("DOCUMENT", "UPDATING/CONFIRMING DOC: $userData")
        repository.updateAndConfirmDocInfo(docId, userData)
            .observeForever {
                processConfirmResponse(it)
            }
    }

    fun getCurrentStage() {
        repository.getCurrentStage().observeForever {
            processStageResponse(it)
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

    private fun processStageResponse(response: Resource<StageResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                stageResponse.value = response
            }
            Resource.Status.ERROR -> {
                if (response.apiError != null) {
                    stageResponse.value = response
                }
            }
        }
    }
}