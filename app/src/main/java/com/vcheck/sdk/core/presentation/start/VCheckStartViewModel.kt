package com.vcheck.sdk.core.presentation.start

import androidx.lifecycle.*
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.*

internal class VCheckStartViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var timestampResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()

    var providersResponse: MutableLiveData<Resource<ProvidersResponse>> = MutableLiveData()

    fun serviceTimestampRequest() {
        repository.getActualServiceTimestamp().observeForever { ts ->
            processTimestampResponse(ts)
        }
    }

    fun initVerification() {
        repository.initVerification().observeForever {
            processInitVerifResponse(it)
        }
    }

    fun getProviders() {
        repository.getProviders().observeForever {
            processGetProvidersResponse(it)
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

    private fun processGetProvidersResponse(response: Resource<ProvidersResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                providersResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }

}