package com.vcheck.demo.dev.presentation.start

import androidx.lifecycle.*
import com.vcheck.demo.dev.VCheckSDK
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.*

internal class DemoStartViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    //var configModel:

    var timestampResponse: MutableLiveData<String> = MutableLiveData()
    var createResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()
    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()
    var countriesResponse: MutableLiveData<Resource<CountriesResponse>> = MutableLiveData()
    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    private lateinit var verifToken: String

    fun setVerifToken(token: String) {
        verifToken = token
    }

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
        repository.initVerification(verifToken).observeForever {
            processInitVerifResponse(it)
        }
    }

    fun getCurrentStage() {
        repository.getCurrentStage(verifToken).observeForever {
            processStageResponse(it)
        }
    }

    fun getCountriesList() {
        repository.getCountries(verifToken).observeForever {
            processGetCountriesResponse(it)
        }
    }

    private fun processTimestampResponse(response: Resource<String>){
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                if (response.data != null) {
                    timestampResponse = MutableLiveData(response.data)
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