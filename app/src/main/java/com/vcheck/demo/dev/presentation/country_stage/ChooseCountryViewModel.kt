package com.vcheck.demo.dev.presentation.country_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.LocalDatasource
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.CountriesResponse

class ChooseCountryViewModel(
    private val repository: MainRepository
) : ViewModel() {

    var countriesResponse: MutableLiveData<Resource<CountriesResponse>> = MutableLiveData()
    val clientError: MutableLiveData<String> = MutableLiveData()
    private lateinit var verifToken: String

    fun setVerifToken(token: String) {
        verifToken = token
    }

    fun getCountriesList() {
        repository.getCountries(verifToken).observeForever {
            processGetCountriesResponse(it)
        }
    }

    private fun processGetCountriesResponse(response: Resource<CountriesResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
                //setLoading()
            }
            Resource.Status.SUCCESS -> {
                //setSuccess()
                countriesResponse.value = response
            }
            Resource.Status.ERROR -> {
                setError(response.apiError!!.errorText)
                //error.value = response.resourceError
            }
        }
    }

    private fun setError(message: String) {
        clientError.value = message
    }
}