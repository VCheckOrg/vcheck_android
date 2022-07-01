package com.vcheck.demo.dev.presentation.start

import androidx.lifecycle.*
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.CountriesResponse
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.StageResponse
import com.vcheck.demo.dev.domain.VerificationInitResponse

internal class DemoStartViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    //TODO should be removed with new architecture!
    var verifResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()

    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()
    var countriesResponse: MutableLiveData<Resource<CountriesResponse>> = MutableLiveData()
    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    private lateinit var verifToken: String

    fun setVerifToken(token: String) {
        verifToken = token
    }

    fun createTestVerificationRequest(deviceDefaultLocaleCode: String) {
        repository.getActualServiceTimestamp().observeForever { ts ->
            processTimestampResponse(ts, deviceDefaultLocaleCode)
        }
    }

    fun initVerification() {
        repository.initVerification(verifToken).observeForever {
            processInitVerifResponse(it)
        }
    }

    fun getCurrentStage() {
        repository.getCurrentStage().observeForever {

        }
    }

    fun getCountriesList() {
        repository.getCountries(verifToken).observeForever {
            processGetCountriesResponse(it)
        }
    }

    private fun processTimestampResponse(response: Resource<String>, deviceDefaultLocaleCode: String){
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                if (response.data != null) {
                    repository.createTestVerificationRequest(response.data.toLong(),
                        deviceDefaultLocaleCode).observeForever {
                            processCreateVerifResponse(it)
                        }
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
                verifResponse.value = response
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