package com.vcheck.demo.dev.presentation.start

import android.util.Log
import androidx.lifecycle.*
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.CountriesResponse
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.VerificationInitResponse

class DemoStartViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var verifResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()
    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()
    var countriesResponse: MutableLiveData<Resource<CountriesResponse>> = MutableLiveData()

    private lateinit var verifToken: String

    fun setVerifToken(token: String) {
        verifToken = token
    }

    fun createTestVerificationRequest() {
        repository.getActualServiceTimestamp().observeForever { ts ->
            if (ts.data != null) {
                repository.createTestVerificationRequest(ts.data.toLong()).observeForever {
                    processCreateVerifResponse(it)
                }
            }
        }
    }

    fun initVerification() {
        repository.initVerification(verifToken).observeForever {
            processInitVerifResponse(it)
        }
    }

    fun getCountriesList() {
        repository.getCountries(verifToken).observeForever {
            processGetCountriesResponse(it)
        }
    }

    private fun processCreateVerifResponse(response: Resource<CreateVerificationAttemptResponse>){
        when(response.status) {
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

    private fun processInitVerifResponse(response: Resource<VerificationInitResponse>){
        when(response.status) {
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

    private fun processGetCountriesResponse(response: Resource<CountriesResponse>){
        when(response.status) {
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