package com.vcheck.sdk.core.presentation.check_doc_info_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.ApiError
import com.vcheck.sdk.core.domain.DocUserDataRequestBody
import com.vcheck.sdk.core.domain.PreProcessedDocumentResponse
import com.vcheck.sdk.core.domain.StageResponse
import retrofit2.Response

class CheckDocInfoViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<ApiError?> = MutableLiveData(null)

    var confirmedDocResponse: MutableLiveData<Resource<Response<Void>>?> = MutableLiveData(null)

    var documentInfoResponse: MutableLiveData<Resource<PreProcessedDocumentResponse>> = MutableLiveData()

    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    val stageSpecificError: MutableLiveData<ApiError?> = MutableLiveData(null)

    fun getDocumentInfo(docId: Int) {
        repository.getDocumentInfo(docId).observeForever {
            documentInfoResponse.value = it
        }
    }

    fun updateAndConfirmDocument(docId: Int, userData: DocUserDataRequestBody) {
        repository.updateAndConfirmDocInfo(docId, userData).observeForever {
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
                if (response.apiError != null) {
                    clientError.value = response.apiError
                }
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
                    stageSpecificError.value = response.apiError
                }
            }
        }
    }
}