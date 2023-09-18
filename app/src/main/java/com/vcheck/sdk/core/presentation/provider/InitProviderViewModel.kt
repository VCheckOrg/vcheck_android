package com.vcheck.sdk.core.presentation.provider

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.ApiError
import com.vcheck.sdk.core.domain.InitProviderRequestBody
import com.vcheck.sdk.core.domain.StageResponse

class InitProviderViewModel (val repository: MainRepository) : ViewModel() {

    var initProviderResponse: MutableLiveData<Resource<Void>> = MutableLiveData(null)

    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    val clientError: MutableLiveData<ApiError?> = MutableLiveData(null)

    val stageSpecificError: MutableLiveData<ApiError?> = MutableLiveData(null)

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

    private fun processInitProviderResponse(response: Resource<Void>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                initProviderResponse.value = response
            }
            Resource.Status.ERROR -> {
                //not handling any of possible custom codes here
                //clientError.value = response.apiError
                initProviderResponse.value = response
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
                stageSpecificError.value = response.apiError
            }
        }
    }
}