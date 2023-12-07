package com.vcheck.sdk.core.presentation.country

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.PriorityCountries

class ChooseCountryViewModel (val repository: MainRepository) : ViewModel() {

    var priorityCountriesResponse: MutableLiveData<Resource<PriorityCountries>> = MutableLiveData()

    fun getPriorityCountries() {
        repository.getPriorityCountries().observeForever {
            processResponse(it)
        }
    }

    private fun processResponse(response: Resource<PriorityCountries>){
        when(response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                priorityCountriesResponse.value = response
            }
            Resource.Status.ERROR -> {
                //clientError.value = response.apiError?.errorText ?: ""
            }
        }
    }
}