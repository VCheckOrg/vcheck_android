package com.vcheck.sdk.core.presentation.provider

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.InitProviderRequestBody
import com.vcheck.sdk.core.domain.StageResponse
import retrofit2.Response

class InitProviderViewModel (val repository: MainRepository) : ViewModel() {

    var initProviderResponse: MutableLiveData<Resource<Response<Void>>?> = MutableLiveData(null)

    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    fun initProvider(initProviderRequestBody: InitProviderRequestBody) {
        repository.initProvider(initProviderRequestBody).observeForever {
            processInitProviderResponse(it)
        }
    }

    fun getCurrentStage() {
        repository.getCurrentStage().observeForever {
            processStageResponse(it)
        }
    }

    private fun processInitProviderResponse(response: Resource<Response<Void>>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                initProviderResponse.value = response
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
                clientError.value = response.apiError!!.errorText
            }
        }
    }
}