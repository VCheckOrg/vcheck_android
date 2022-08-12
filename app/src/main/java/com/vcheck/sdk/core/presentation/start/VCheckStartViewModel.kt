package com.vcheck.sdk.core.presentation.start

import androidx.lifecycle.*
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.*

internal class VCheckStartViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var timestampResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    var createResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()
    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()
    var countriesResponse: MutableLiveData<Resource<CountriesResponse>> = MutableLiveData()
    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    fun serviceTimestampRequest() {
        repository.getActualServiceTimestamp().observeForever { ts ->
            processTimestampResponse(ts)
        }
    }

    fun createVerificationRequest(createVerificationRequestBody: CreateVerificationRequestBody) {
        repository.createVerification(createVerificationRequestBody).observeForever {
            processCreateVerifResponse(it)
        }
    }

    fun initVerification() {
        repository.initVerification().observeForever {
            processInitVerifResponse(it)
        }
    }

    fun getCurrentStage() {
        repository.getCurrentStage().observeForever {
            processStageResponse(it)
        }
    }

    fun getCountriesList() {
        repository.getCountries().observeForever {
            processGetCountriesResponse(it)
        }
    }

    private fun processTimestampResponse(response: Resource<String>){
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                if (response.data != null) {
                    timestampResponse.value = response
                }
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }

    private fun processCreateVerifResponse(response: Resource<CreateVerificationAttemptResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                createResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }

    private fun processInitVerifResponse(response: Resource<VerificationInitResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                initResponse.value = response
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

    private fun processGetCountriesResponse(response: Resource<CountriesResponse>){
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                countriesResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }
}