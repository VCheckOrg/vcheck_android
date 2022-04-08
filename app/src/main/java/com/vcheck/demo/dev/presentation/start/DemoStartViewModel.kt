package com.vcheck.demo.dev.presentation.start

import androidx.lifecycle.*
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.CountriesResponse
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.VerificationInitResponse

class DemoStartViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)
    //    private val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
    //    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    var verifResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()
    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()
    var countriesResponse: MutableLiveData<Resource<CountriesResponse>> = MutableLiveData()

    private lateinit var verifToken: String

    fun setVerifToken(token: String) {
        verifToken = token
    }

    fun createTestVerificationRequest() {
        repository.createTestVerificationRequest().observeForever {
            processCreateVerifResponse(it)
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
                //setLoading()
            }
            Resource.Status.SUCCESS -> {
                //setSuccess()
                countriesResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }
}